package com.nextstep.app.data.sync

import com.nextstep.app.data.local.EventEntity
import com.nextstep.app.data.local.GradeEntity
import com.nextstep.app.data.local.MemberEntity
import com.nextstep.app.data.local.NoteEntity
import com.nextstep.app.data.local.StudySessionEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.local.TaskEntity
import com.nextstep.app.data.local.TopicEntity
import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.ExamType
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus

/**
 * Room 엔티티 <-> Firestore 문서(Map) 변환.
 * 리플렉션 기반 직렬화 대신 명시적으로 매핑해 난독화/스키마 변경에 안전하게 합니다.
 * `dirty` 는 기기 로컬 상태이므로 서버에 올리지 않습니다.
 */
object Mappers {
    private fun Map<String, Any?>.str(key: String, default: String = ""): String = this[key] as? String ?: default
    private fun Map<String, Any?>.strOrNull(key: String): String? = this[key] as? String
    private fun Map<String, Any?>.long(key: String, default: Long = 0L): Long = (this[key] as? Number)?.toLong() ?: default
    private fun Map<String, Any?>.int(key: String, default: Int = 0): Int = (this[key] as? Number)?.toInt() ?: default
    private fun Map<String, Any?>.dbl(key: String, default: Double = 0.0): Double = (this[key] as? Number)?.toDouble() ?: default
    private fun Map<String, Any?>.dblOrNull(key: String): Double? = (this[key] as? Number)?.toDouble()
    private fun Map<String, Any?>.bool(key: String, default: Boolean = false): Boolean = this[key] as? Boolean ?: default

    fun subjectToMap(e: SubjectEntity): Map<String, Any?> = mapOf(
        "id" to e.id, "familyId" to e.familyId, "name" to e.name, "color" to e.color,
        "teacher" to e.teacher, "weeklyGoalMinutes" to e.weeklyGoalMinutes, "orderIndex" to e.orderIndex,
        "updatedAt" to e.updatedAt, "deleted" to e.deleted,
    )

    fun subjectFromMap(id: String, m: Map<String, Any?>): SubjectEntity = SubjectEntity(
        id = id, familyId = m.str("familyId"), name = m.str("name"), color = m.long("color", 0xFF4F46E5),
        teacher = m.str("teacher"), weeklyGoalMinutes = m.int("weeklyGoalMinutes"), orderIndex = m.int("orderIndex"),
        updatedAt = m.long("updatedAt"), deleted = m.bool("deleted"), dirty = false,
    )

    fun topicToMap(e: TopicEntity): Map<String, Any?> = mapOf(
        "id" to e.id, "familyId" to e.familyId, "subjectId" to e.subjectId, "title" to e.title,
        "orderIndex" to e.orderIndex, "classCovered" to e.classCovered, "status" to e.status.name,
        "confidence" to e.confidence, "updatedAt" to e.updatedAt, "deleted" to e.deleted,
    )

    fun topicFromMap(id: String, m: Map<String, Any?>): TopicEntity = TopicEntity(
        id = id, familyId = m.str("familyId"), subjectId = m.str("subjectId"), title = m.str("title"),
        orderIndex = m.int("orderIndex"), classCovered = m.bool("classCovered"),
        status = TopicStatus.from(m.strOrNull("status")), confidence = m.int("confidence"),
        updatedAt = m.long("updatedAt"), deleted = m.bool("deleted"), dirty = false,
    )

    fun taskToMap(e: TaskEntity): Map<String, Any?> = mapOf(
        "id" to e.id, "familyId" to e.familyId, "subjectId" to e.subjectId, "topicId" to e.topicId,
        "title" to e.title, "type" to e.type.name, "dueDate" to e.dueDate, "done" to e.done,
        "createdByRole" to e.createdByRole, "note" to e.note, "updatedAt" to e.updatedAt, "deleted" to e.deleted,
    )

    fun taskFromMap(id: String, m: Map<String, Any?>): TaskEntity = TaskEntity(
        id = id, familyId = m.str("familyId"), subjectId = m.strOrNull("subjectId"), topicId = m.strOrNull("topicId"),
        title = m.str("title"), type = TaskType.from(m.strOrNull("type")), dueDate = m.long("dueDate"),
        done = m.bool("done"), createdByRole = m.str("createdByRole"), note = m.str("note"),
        updatedAt = m.long("updatedAt"), deleted = m.bool("deleted"), dirty = false,
    )

    fun eventToMap(e: EventEntity): Map<String, Any?> = mapOf(
        "id" to e.id, "familyId" to e.familyId, "subjectId" to e.subjectId, "title" to e.title,
        "type" to e.type.name, "startAt" to e.startAt, "endAt" to e.endAt, "repeatWeekly" to e.repeatWeekly,
        "location" to e.location, "memo" to e.memo, "updatedAt" to e.updatedAt, "deleted" to e.deleted,
    )

    fun eventFromMap(id: String, m: Map<String, Any?>): EventEntity = EventEntity(
        id = id, familyId = m.str("familyId"), subjectId = m.strOrNull("subjectId"), title = m.str("title"),
        type = EventType.from(m.strOrNull("type")), startAt = m.long("startAt"), endAt = m.long("endAt"),
        repeatWeekly = m.bool("repeatWeekly"), location = m.str("location"), memo = m.str("memo"),
        updatedAt = m.long("updatedAt"), deleted = m.bool("deleted"), dirty = false,
    )

    fun gradeToMap(e: GradeEntity): Map<String, Any?> = mapOf(
        "id" to e.id, "familyId" to e.familyId, "subjectId" to e.subjectId, "title" to e.title,
        "examType" to e.examType.name, "score" to e.score, "maxScore" to e.maxScore,
        "classAverage" to e.classAverage, "date" to e.date, "memo" to e.memo,
        "updatedAt" to e.updatedAt, "deleted" to e.deleted,
    )

    fun gradeFromMap(id: String, m: Map<String, Any?>): GradeEntity = GradeEntity(
        id = id, familyId = m.str("familyId"), subjectId = m.str("subjectId"), title = m.str("title"),
        examType = ExamType.from(m.strOrNull("examType")), score = m.dbl("score"), maxScore = m.dbl("maxScore", 100.0),
        classAverage = m.dblOrNull("classAverage"), date = m.long("date"), memo = m.str("memo"),
        updatedAt = m.long("updatedAt"), deleted = m.bool("deleted"), dirty = false,
    )

    fun sessionToMap(e: StudySessionEntity): Map<String, Any?> = mapOf(
        "id" to e.id, "familyId" to e.familyId, "subjectId" to e.subjectId, "startAt" to e.startAt,
        "endAt" to e.endAt, "durationMinutes" to e.durationMinutes, "note" to e.note, "fromTimer" to e.fromTimer,
        "updatedAt" to e.updatedAt, "deleted" to e.deleted,
    )

    fun sessionFromMap(id: String, m: Map<String, Any?>): StudySessionEntity = StudySessionEntity(
        id = id, familyId = m.str("familyId"), subjectId = m.strOrNull("subjectId"), startAt = m.long("startAt"),
        endAt = m.long("endAt"), durationMinutes = m.int("durationMinutes"), note = m.str("note"),
        fromTimer = m.bool("fromTimer"), updatedAt = m.long("updatedAt"), deleted = m.bool("deleted"), dirty = false,
    )

    fun noteToMap(e: NoteEntity): Map<String, Any?> = mapOf(
        "id" to e.id, "familyId" to e.familyId, "authorRole" to e.authorRole, "authorName" to e.authorName,
        "text" to e.text, "createdAt" to e.createdAt, "updatedAt" to e.updatedAt, "deleted" to e.deleted,
    )

    fun memberToMap(e: MemberEntity): Map<String, Any?> = mapOf(
        "id" to e.id, "familyId" to e.familyId, "role" to e.role, "name" to e.name, "title" to e.title,
        "subjectIds" to e.subjectIds, "joinedAt" to e.joinedAt, "updatedAt" to e.updatedAt, "deleted" to e.deleted,
    )

    fun memberFromMap(id: String, m: Map<String, Any?>): MemberEntity = MemberEntity(
        id = id, familyId = m.str("familyId"), role = m.str("role"), name = m.str("name"), title = m.str("title"),
        subjectIds = m.str("subjectIds"), joinedAt = m.long("joinedAt"), updatedAt = m.long("updatedAt"),
        deleted = m.bool("deleted"), dirty = false,
    )

    fun noteFromMap(id: String, m: Map<String, Any?>): NoteEntity = NoteEntity(
        id = id, familyId = m.str("familyId"), authorRole = m.str("authorRole"), authorName = m.str("authorName"),
        text = m.str("text"), createdAt = m.long("createdAt"), updatedAt = m.long("updatedAt"),
        deleted = m.bool("deleted"), dirty = false,
    )
}
