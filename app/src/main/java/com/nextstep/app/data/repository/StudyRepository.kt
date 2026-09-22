package com.nextstep.app.data.repository

import com.nextstep.app.data.local.AppDatabase
import com.nextstep.app.data.local.EventEntity
import com.nextstep.app.data.local.GradeEntity
import com.nextstep.app.data.local.MemberEntity
import com.nextstep.app.data.local.NoteEntity
import com.nextstep.app.data.local.RoadmapItemEntity
import com.nextstep.app.data.local.StudySessionEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.local.TaskEntity
import com.nextstep.app.data.local.TopicEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.data.prefs.RunningTimer
import com.nextstep.app.data.prefs.UserPreferences
import com.nextstep.app.data.prefs.UserProfile
import com.nextstep.app.data.sync.FamilyInfo
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Locale
import kotlin.random.Random

/**
 * 앱의 단일 데이터 진입점. Room 을 읽고 쓰며, 쓰기 후에는 SyncManager 에 전송을 요청합니다.
 * ViewModel 은 이 클래스만 알면 됩니다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StudyRepository(
    private val db: AppDatabase,
    private val prefs: UserPreferences,
    val sync: SyncManager,
) {
    val profile: Flow<UserProfile> = prefs.profile
    val familyId: Flow<String?> = profile.map { it.familyId }.distinctUntilChanged()
    val runningTimer: Flow<RunningTimer?> = prefs.runningTimer

    private fun <T> withFamily(block: (String) -> Flow<List<T>>): Flow<List<T>> =
        familyId.flatMapLatest { id -> if (id == null) flowOf(emptyList()) else block(id) }

    val subjects: Flow<List<SubjectEntity>> = withFamily { db.subjectDao().observeAll(it) }
    val topics: Flow<List<TopicEntity>> = withFamily { db.topicDao().observeAll(it) }
    val tasks: Flow<List<TaskEntity>> = withFamily { db.taskDao().observeAll(it) }
    val events: Flow<List<EventEntity>> = withFamily { db.eventDao().observeAll(it) }
    val grades: Flow<List<GradeEntity>> = withFamily { db.gradeDao().observeAll(it) }
    val sessions: Flow<List<StudySessionEntity>> = withFamily { db.studySessionDao().observeAll(it) }
    val notes: Flow<List<NoteEntity>> = withFamily { db.noteDao().observeAll(it) }
    val members: Flow<List<MemberEntity>> = withFamily { db.memberDao().observeAll(it) }
    val roadmap: Flow<List<RoadmapItemEntity>> = withFamily { db.roadmapDao().observeAll(it) }

    /** 이 기기 사용자의 구성원 정보(멘토라면 담당 과목 포함). */
    val myMember: Flow<MemberEntity?> = profile.map { it.memberId }.distinctUntilChanged()
        .flatMapLatest { id -> if (id == null) flowOf(null) else db.memberDao().observeById(id) }

    fun observeSubject(id: String): Flow<SubjectEntity?> = db.subjectDao().observeById(id)
    fun observeTopics(subjectId: String): Flow<List<TopicEntity>> = db.topicDao().observeBySubject(subjectId)

    private suspend fun requireFamilyId(): String =
        profile.first().familyId ?: error("가족 정보가 설정되지 않았습니다")

    private fun now() = System.currentTimeMillis()

    // ---------------------------------------------------------------- onboarding

    /** 학생: 새 가족을 만들고 페어링 코드를 발급합니다. */
    suspend fun createFamilyAsStudent(studentName: String): Result<FamilyInfo> {
        val info = FamilyInfo(
            familyId = com.nextstep.app.data.local.newId(),
            pairingCode = generatePairingCode(),
            studentName = studentName,
        )
        val remote = sync.createFamily(info)
        if (remote.isFailure) return Result.failure(remote.exceptionOrNull() ?: IllegalStateException("서버 오류"))
        val member = MemberEntity(familyId = info.familyId, role = Role.STUDENT.name, name = studentName)
        db.memberDao().upsert(member)
        prefs.completeOnboarding(Role.STUDENT, studentName, info.familyId, info.pairingCode, studentName, member.id)
        seedDefaultSubjects(info.familyId)
        sync.start(info.familyId)
        return Result.success(info)
    }

    /**
     * 학부모/멘토: 페어링 코드로 학생의 가족에 참여합니다.
     * 같은 코드로 여러 명(학부모 여러 명, 멘토 여러 명)이 참여할 수 있으며 각자 구성원 행을 하나씩 가집니다.
     */
    suspend fun joinFamily(role: Role, name: String, code: String, title: String = ""): Result<FamilyInfo> {
        require(role != Role.STUDENT) { "학생은 코드로 참여할 수 없습니다" }
        val normalized = code.trim().uppercase(Locale.ROOT)
        if (!sync.isAvailable) {
            return Result.failure(IllegalStateException("동기화 서버가 설정되지 않아 학생 기기와 연결할 수 없습니다. README 의 Firebase 설정을 참고하세요."))
        }
        val found = sync.findFamilyByCode(normalized)
        val info = found.getOrElse { return Result.failure(it) }
            ?: return Result.failure(IllegalArgumentException("코드에 해당하는 학생을 찾지 못했습니다"))
        val member = MemberEntity(familyId = info.familyId, role = role.name, name = name, title = title, mentorEnabled = role == Role.MENTOR)
        db.memberDao().upsert(member)
        prefs.completeOnboarding(role, name, info.familyId, info.pairingCode, info.studentName, member.id)
        sync.start(info.familyId)
        return Result.success(info)
    }

    // ---------------------------------------------------------------- members

    /** 멘토의 담당 과목을 설정합니다. 비우면 전 과목 담당. */
    suspend fun setMemberSubjects(memberId: String, subjectIds: List<String>) {
        val m = db.memberDao().getById(memberId) ?: return
        db.memberDao().upsert(m.copy(subjectIds = subjectIds.joinToString(","), updatedAt = now(), dirty = true))
        sync.requestPush()
    }

    suspend fun updateMemberProfile(memberId: String, name: String, title: String) {
        val m = db.memberDao().getById(memberId) ?: return
        db.memberDao().upsert(m.copy(name = name, title = title, updatedAt = now(), dirty = true))
        sync.requestPush()
    }

    /** 학부모가 멘토 역할을 겸할지 설정합니다. 멘토 본인은 항상 켜져 있습니다. */
    suspend fun setMentorEnabled(memberId: String, enabled: Boolean) {
        val m = db.memberDao().getById(memberId) ?: return
        val value = if (m.role == Role.MENTOR.name) true else enabled
        db.memberDao().upsert(m.copy(mentorEnabled = value, updatedAt = now(), dirty = true))
        sync.requestPush()
    }

    // ---------------------------------------------------------------- roadmap

    suspend fun saveRoadmapItem(item: RoadmapItemEntity) {
        val p = profile.first()
        val withAuthor = if (item.createdByName.isBlank()) item.copy(createdByName = p.displayName, createdByRole = p.role?.name ?: "") else item
        db.roadmapDao().upsert(withAuthor.copy(familyId = withAuthor.familyId.ifEmpty { requireFamilyId() }, updatedAt = now(), dirty = true))
        sync.requestPush()
    }

    suspend fun setRoadmapStatus(id: String, status: RoadmapStatus) {
        val r = db.roadmapDao().getById(id) ?: return
        saveRoadmapItem(r.copy(status = status))
    }

    suspend fun deleteRoadmapItem(id: String) {
        val r = db.roadmapDao().getById(id) ?: return
        saveRoadmapItem(r.copy(deleted = true))
    }

    /** 학습 계획 생성기가 만든 일정과 할 일을 한 번에 저장합니다. */
    suspend fun applyStudyPlan(events: List<EventEntity>, tasks: List<TaskEntity>) {
        val familyId = requireFamilyId()
        val ts = now()
        db.eventDao().upsertAll(events.map { it.copy(familyId = familyId, updatedAt = ts, dirty = true) })
        db.taskDao().upsertAll(tasks.map { it.copy(familyId = familyId, updatedAt = ts, dirty = true) })
        sync.requestPush()
    }

    /** 학생/학부모가 연결된 구성원(멘토 등)을 목록에서 제거합니다. */
    suspend fun removeMember(memberId: String) {
        val m = db.memberDao().getById(memberId) ?: return
        db.memberDao().upsert(m.copy(deleted = true, updatedAt = now(), dirty = true))
        sync.requestPush()
    }

    /** 앱 시작 시 저장된 가족이 있으면 동기화를 켭니다. */
    suspend fun resumeSync() {
        profile.first().familyId?.let { sync.start(it) }
    }

    suspend fun signOut() {
        sync.stop()
        prefs.reset()
    }

    private fun generatePairingCode(): String {
        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { alphabet[Random.nextInt(alphabet.length)] }.joinToString("")
    }

    private suspend fun seedDefaultSubjects(familyId: String) {
        if (db.subjectDao().count(familyId) > 0) return
        val defaults = listOf(
            "국어" to 0xFFEF4444, "수학" to 0xFF3B82F6, "영어" to 0xFF10B981,
            "과학" to 0xFF8B5CF6, "사회" to 0xFFF59E0B,
        )
        db.subjectDao().upsertAll(
            defaults.mapIndexed { i, (name, color) ->
                SubjectEntity(familyId = familyId, name = name, color = color, orderIndex = i, weeklyGoalMinutes = 180)
            },
        )
        sync.requestPush()
    }

    // ---------------------------------------------------------------- subjects

    suspend fun saveSubject(subject: SubjectEntity) {
        db.subjectDao().upsert(subject.copy(familyId = subject.familyId.ifEmpty { requireFamilyId() }, updatedAt = now(), dirty = true))
        sync.requestPush()
    }

    suspend fun deleteSubject(id: String) {
        val s = db.subjectDao().getById(id) ?: return
        db.subjectDao().upsert(s.copy(deleted = true, updatedAt = now(), dirty = true))
        db.topicDao().getBySubject(id).forEach { t ->
            db.topicDao().upsert(t.copy(deleted = true, updatedAt = now(), dirty = true))
        }
        sync.requestPush()
    }

    // ---------------------------------------------------------------- topics

    suspend fun addTopic(subjectId: String, title: String) {
        val existing = db.topicDao().getBySubject(subjectId)
        db.topicDao().upsert(
            TopicEntity(familyId = requireFamilyId(), subjectId = subjectId, title = title, orderIndex = existing.size),
        )
        sync.requestPush()
    }

    suspend fun addTopics(subjectId: String, titles: List<String>) {
        val existing = db.topicDao().getBySubject(subjectId).size
        val familyId = requireFamilyId()
        db.topicDao().upsertAll(
            titles.filter { it.isNotBlank() }.mapIndexed { i, t ->
                TopicEntity(familyId = familyId, subjectId = subjectId, title = t.trim(), orderIndex = existing + i)
            },
        )
        sync.requestPush()
    }

    suspend fun updateTopic(topic: TopicEntity) {
        db.topicDao().upsert(topic.copy(updatedAt = now(), dirty = true))
        sync.requestPush()
    }

    suspend fun setTopicStatus(id: String, status: TopicStatus) {
        val t = db.topicDao().getById(id) ?: return
        updateTopic(t.copy(status = status))
    }

    /** 학급 진도 갱신: 해당 단원까지(포함) 모두 수업에서 다룬 것으로 표시합니다. */
    suspend fun setClassProgress(subjectId: String, upToOrderIndex: Int) {
        val all = db.topicDao().getBySubject(subjectId)
        val ts = now()
        db.topicDao().upsertAll(
            all.map { t ->
                val covered = t.orderIndex <= upToOrderIndex
                val status = if (covered && t.status.order < TopicStatus.IN_CLASS.order) TopicStatus.IN_CLASS else t.status
                t.copy(classCovered = covered, status = status, updatedAt = ts, dirty = true)
            },
        )
        sync.requestPush()
    }

    suspend fun deleteTopic(id: String) {
        val t = db.topicDao().getById(id) ?: return
        db.topicDao().upsert(t.copy(deleted = true, updatedAt = now(), dirty = true))
        sync.requestPush()
    }

    // ---------------------------------------------------------------- tasks

    suspend fun saveTask(task: TaskEntity) {
        db.taskDao().upsert(task.copy(familyId = task.familyId.ifEmpty { requireFamilyId() }, updatedAt = now(), dirty = true))
        sync.requestPush()
    }

    suspend fun setTaskDone(id: String, done: Boolean) {
        val t = db.taskDao().getById(id) ?: return
        saveTask(t.copy(done = done))
    }

    suspend fun deleteTask(id: String) {
        val t = db.taskDao().getById(id) ?: return
        saveTask(t.copy(deleted = true))
    }

    // ---------------------------------------------------------------- events

    suspend fun saveEvent(event: EventEntity) {
        db.eventDao().upsert(event.copy(familyId = event.familyId.ifEmpty { requireFamilyId() }, updatedAt = now(), dirty = true))
        sync.requestPush()
    }

    suspend fun deleteEvent(id: String) {
        val e = db.eventDao().getById(id) ?: return
        saveEvent(e.copy(deleted = true))
    }

    // ---------------------------------------------------------------- grades

    suspend fun saveGrade(grade: GradeEntity) {
        db.gradeDao().upsert(grade.copy(familyId = grade.familyId.ifEmpty { requireFamilyId() }, updatedAt = now(), dirty = true))
        sync.requestPush()
    }

    suspend fun deleteGrade(id: String) {
        val g = db.gradeDao().getById(id) ?: return
        saveGrade(g.copy(deleted = true))
    }

    // ---------------------------------------------------------------- study sessions & timer

    suspend fun saveSession(session: StudySessionEntity) {
        db.studySessionDao().upsert(session.copy(familyId = session.familyId.ifEmpty { requireFamilyId() }, updatedAt = now(), dirty = true))
        sync.requestPush()
    }

    suspend fun deleteSession(id: String) {
        val s = db.studySessionDao().getById(id) ?: return
        saveSession(s.copy(deleted = true))
    }

    suspend fun startTimer(subjectId: String?) {
        prefs.startTimer(subjectId, now())
    }

    /** 타이머를 멈추고 1분 이상이면 학습 기록으로 저장합니다. 저장된 세션을 반환합니다. */
    suspend fun stopTimer(note: String = ""): StudySessionEntity? {
        val timer = prefs.runningTimer.first() ?: return null
        prefs.clearTimer()
        val end = now()
        val minutes = ((end - timer.startedAt) / 60_000L).toInt()
        if (minutes < 1) return null
        val session = StudySessionEntity(
            familyId = requireFamilyId(),
            subjectId = timer.subjectId,
            startAt = timer.startedAt,
            endAt = end,
            durationMinutes = minutes,
            note = note,
            fromTimer = true,
        )
        saveSession(session)
        return session
    }

    suspend fun cancelTimer() = prefs.clearTimer()

    // ---------------------------------------------------------------- notes

    suspend fun addNote(text: String) {
        val p = profile.first()
        db.noteDao().upsert(
            NoteEntity(
                familyId = requireFamilyId(),
                authorRole = p.role?.name ?: Role.STUDENT.name,
                authorName = p.displayName,
                text = text.trim(),
            ),
        )
        sync.requestPush()
    }

    suspend fun deleteNote(id: String) {
        val n = db.noteDao().getById(id) ?: return
        db.noteDao().upsert(n.copy(deleted = true, updatedAt = now(), dirty = true))
        sync.requestPush()
    }
}
