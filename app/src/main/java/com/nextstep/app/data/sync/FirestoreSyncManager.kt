package com.nextstep.app.data.sync

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.nextstep.app.data.model.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Firestore 기반 동기화. 엔티티를 모르고 [SyncedCollection] 만 다룹니다.
 *
 * 구조: families/{familyId}/{collection}/{id}, 공용 저장소는 최상위 catalog/{id}.
 * 정책: 오프라인 우선. Room 이 진실의 원천이고 로컬 변경은 dirty 플래그로 추적해 올립니다.
 * 원격 변경은 updatedAt 이 더 최신일 때만 반영합니다(last-write-wins). 전송 실패는 지수 백오프로 재시도합니다.
 */
@OptIn(FlowPreview::class)
class FirestoreSyncManager(
    private val familyCollections: List<SyncedCollection<*>>,
    private val catalogCollection: SyncedCollection<*>,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : SyncManager {

    private val pushRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val listeners = mutableListOf<ListenerRegistration>()
    private var pushJob: Job? = null
    private var familyId: String? = null

    override val status = MutableStateFlow(SyncStatus.CONNECTING)
    override val isAvailable: Boolean = true

    override suspend fun createFamily(info: FamilyInfo): Result<Unit> = runCatching {
        check(ensureSignedIn()) { "로그인에 실패했습니다" }
        firestore.collection(FAMILIES).document(info.familyId)
            .set(mapOf("pairingCode" to info.pairingCode, "studentName" to info.studentName, "createdAt" to System.currentTimeMillis()))
            .await()
    }

    override suspend fun findFamilyByCode(code: String): Result<FamilyInfo?> = runCatching {
        check(ensureSignedIn()) { "로그인에 실패했습니다" }
        val doc = firestore.collection(FAMILIES).whereEqualTo("pairingCode", code).limit(1).get().await().documents.firstOrNull()
            ?: return@runCatching null
        FamilyInfo(familyId = doc.id, pairingCode = doc.getString("pairingCode") ?: code, studentName = doc.getString("studentName") ?: "")
    }

    override fun start(familyId: String) {
        if (this.familyId == familyId && listeners.isNotEmpty()) return
        stop()
        this.familyId = familyId
        status.value = SyncStatus.CONNECTING
        scope.launch {
            if (!ensureSignedIn()) return@launch
            val family = firestore.collection(FAMILIES).document(familyId)
            familyCollections.forEach { listen(family.collection(it.name), it) }
            listen(firestore.collection(catalogCollection.name), catalogCollection)
            pushJob = launch { pushRequests.debounce(PUSH_DEBOUNCE_MS).collect { pushWithRetry(familyId) } }
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

    private suspend fun ensureSignedIn(): Boolean = try {
        if (auth.currentUser == null) auth.signInAnonymously().await()
        true
    } catch (e: Exception) {
        Log.w(TAG, "anonymous sign-in failed", e)
        status.value = SyncStatus.ERROR
        false
    }

    private fun listen(ref: CollectionReference, collection: SyncedCollection<*>) {
        listeners += ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "listen ${collection.name} failed", error)
                status.value = SyncStatus.ERROR
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener
            val changes = snapshot.documentChanges.filter { it.type != DocumentChange.Type.REMOVED }
            scope.launch {
                changes.forEach { collection.mergeRemote(it.document.id, it.document.data) }
                if (!snapshot.metadata.isFromCache) status.value = SyncStatus.SYNCED
            }
        }
    }

    private suspend fun pushWithRetry(familyId: String) {
        var delayMs = RETRY_BASE_MS
        repeat(MAX_PUSH_ATTEMPTS) { attempt ->
            try {
                pushAll(familyId)
                if (status.value != SyncStatus.SYNCED) status.value = SyncStatus.SYNCED
                return
            } catch (e: Exception) {
                Log.w(TAG, "push attempt ${attempt + 1} failed", e)
                if (attempt == MAX_PUSH_ATTEMPTS - 1) { status.value = SyncStatus.ERROR; return }
                delay(delayMs)
                delayMs *= 2
            }
        }
    }

    private suspend fun pushAll(familyId: String) {
        val family = firestore.collection(FAMILIES).document(familyId)
        familyCollections.forEach { collection ->
            collection.pushDirty(familyId) { docs ->
                val batch = firestore.batch()
                docs.forEach { (id, data) -> batch.set(family.collection(collection.name).document(id), data) }
                batch.commit().await()
            }
        }
    }

    companion object {
        private const val TAG = "FirestoreSync"
        private const val FAMILIES = "families"
        private const val PUSH_DEBOUNCE_MS = 400L
        private const val MAX_PUSH_ATTEMPTS = 3
        private const val RETRY_BASE_MS = 1_000L
    }
}
