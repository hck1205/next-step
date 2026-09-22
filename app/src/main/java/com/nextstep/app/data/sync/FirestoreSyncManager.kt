package com.nextstep.app.data.sync

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.nextstep.app.data.local.AppDatabase
import com.nextstep.app.data.local.Syncable
import com.nextstep.app.data.model.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Firestore 기반 동기화.
 *
 * 구조:
 *   families/{familyId}                      -> { pairingCode, studentName, createdAt }
 *   families/{familyId}/subjects/{id}        -> SubjectEntity
 *   families/{familyId}/topics/{id}          -> TopicEntity
 *   ... tasks, events, grades, sessions, notes
 *
 * 정책: 오프라인 우선. Room 이 진실의 원천이고, 로컬 변경은 dirty 플래그로 추적해 올립니다.
 * 원격 변경은 updatedAt 이 더 최신일 때만 로컬에 반영합니다(last-write-wins).
 */
@OptIn(FlowPreview::class)
class FirestoreSyncManager(
    private val db: AppDatabase,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : SyncManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val pushRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val listeners = mutableListOf<ListenerRegistration>()
    private var pushJob: Job? = null
    private var familyId: String? = null

    override val status: MutableStateFlow<SyncStatus> = MutableStateFlow(SyncStatus.CONNECTING)
    override val isAvailable: Boolean = true

    private suspend fun ensureSignedIn(): Boolean = try {
        if (auth.currentUser == null) auth.signInAnonymously().await()
        true
    } catch (e: Exception) {
        Log.w(TAG, "anonymous sign-in failed", e)
        status.value = SyncStatus.ERROR
        false
    }

    override suspend fun createFamily(info: FamilyInfo): Result<Unit> = runCatching {
        check(ensureSignedIn()) { "로그인에 실패했습니다" }
        firestore.collection(FAMILIES).document(info.familyId).set(
            mapOf(
                "pairingCode" to info.pairingCode,
                "studentName" to info.studentName,
                "createdAt" to System.currentTimeMillis(),
            ),
        ).await()
    }

    override suspend fun findFamilyByCode(code: String): Result<FamilyInfo?> = runCatching {
        check(ensureSignedIn()) { "로그인에 실패했습니다" }
        val snap = firestore.collection(FAMILIES).whereEqualTo("pairingCode", code).limit(1).get().await()
        val doc = snap.documents.firstOrNull() ?: return@runCatching null
        FamilyInfo(
            familyId = doc.id,
            pairingCode = doc.getString("pairingCode") ?: code,
            studentName = doc.getString("studentName") ?: "",
        )
    }

    override fun start(familyId: String) {
        if (this.familyId == familyId && listeners.isNotEmpty()) return
        stop()
        this.familyId = familyId
        status.value = SyncStatus.CONNECTING
        scope.launch {
            if (!ensureSignedIn()) return@launch
            attachListeners(familyId)
            pushJob = launch {
                pushRequests.debounce(400).collect { pushDirty(familyId) }
            }
            pushRequests.tryEmit(Unit)
        }
    }

    override fun stop() {
        listeners.forEach { it.remove() }
        listeners.clear()
        pushJob?.cancel()
        pushJob = null
        familyId = null
    }

    override fun requestPush() {
        pushRequests.tryEmit(Unit)
    }

    private fun attachListeners(familyId: String) {
        val family = firestore.collection(FAMILIES).document(familyId)

        fun <T : Syncable> listen(
            collection: String,
            fromMap: (String, Map<String, Any?>) -> T,
            getLocal: suspend (String) -> T?,
            upsert: suspend (T) -> Unit,
        ) {
            val reg = family.collection(collection).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "listen $collection failed", error)
                    status.value = SyncStatus.ERROR
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                val changes = snapshot.documentChanges.filter { it.type != DocumentChange.Type.REMOVED }
                scope.launch {
                    for (change in changes) {
                        val remote = runCatching { fromMap(change.document.id, change.document.data) }.getOrNull() ?: continue
                        val local = getLocal(remote.id)
                        // 로컬에 아직 올리지 못한 변경(dirty)이 더 최신이면 원격 값으로 덮어쓰지 않습니다.
                        if (local == null || remote.updatedAt > local.updatedAt) upsert(remote)
                    }
                    if (!snapshot.metadata.isFromCache) status.value = SyncStatus.SYNCED
                }
            }
            listeners += reg
        }

        listen(SUBJECTS, Mappers::subjectFromMap, db.subjectDao()::getById, db.subjectDao()::upsert)
        listen(TOPICS, Mappers::topicFromMap, db.topicDao()::getById, db.topicDao()::upsert)
        listen(TASKS, Mappers::taskFromMap, db.taskDao()::getById, db.taskDao()::upsert)
        listen(EVENTS, Mappers::eventFromMap, db.eventDao()::getById, db.eventDao()::upsert)
        listen(GRADES, Mappers::gradeFromMap, db.gradeDao()::getById, db.gradeDao()::upsert)
        listen(SESSIONS, Mappers::sessionFromMap, db.studySessionDao()::getById, db.studySessionDao()::upsert)
        listen(NOTES, Mappers::noteFromMap, db.noteDao()::getById, db.noteDao()::upsert)
        listen(MEMBERS, Mappers::memberFromMap, db.memberDao()::getById, db.memberDao()::upsert)
    }

    private suspend fun pushDirty(familyId: String) {
        try {
            val family = firestore.collection(FAMILIES).document(familyId)

            suspend fun <T : Syncable> push(
                collection: String,
                items: List<T>,
                toMap: (T) -> Map<String, Any?>,
                markClean: suspend (List<String>) -> Unit,
            ) {
                if (items.isEmpty()) return
                items.chunked(400).forEach { chunk ->
                    val batch = firestore.batch()
                    chunk.forEach { item -> batch.set(family.collection(collection).document(item.id), toMap(item)) }
                    batch.commit().await()
                    markClean(chunk.map { it.id })
                }
            }

            push(SUBJECTS, db.subjectDao().getDirty(familyId), Mappers::subjectToMap, db.subjectDao()::markClean)
            push(TOPICS, db.topicDao().getDirty(familyId), Mappers::topicToMap, db.topicDao()::markClean)
            push(TASKS, db.taskDao().getDirty(familyId), Mappers::taskToMap, db.taskDao()::markClean)
            push(EVENTS, db.eventDao().getDirty(familyId), Mappers::eventToMap, db.eventDao()::markClean)
            push(GRADES, db.gradeDao().getDirty(familyId), Mappers::gradeToMap, db.gradeDao()::markClean)
            push(SESSIONS, db.studySessionDao().getDirty(familyId), Mappers::sessionToMap, db.studySessionDao()::markClean)
            push(NOTES, db.noteDao().getDirty(familyId), Mappers::noteToMap, db.noteDao()::markClean)
            push(MEMBERS, db.memberDao().getDirty(familyId), Mappers::memberToMap, db.memberDao()::markClean)
            if (status.value != SyncStatus.SYNCED) status.value = SyncStatus.SYNCED
        } catch (e: Exception) {
            Log.w(TAG, "push failed", e)
            status.value = SyncStatus.ERROR
        }
    }

    companion object {
        private const val TAG = "FirestoreSync"
        const val FAMILIES = "families"
        const val SUBJECTS = "subjects"
        const val TOPICS = "topics"
        const val TASKS = "tasks"
        const val EVENTS = "events"
        const val GRADES = "grades"
        const val SESSIONS = "sessions"
        const val NOTES = "notes"
        const val MEMBERS = "members"
    }
}
