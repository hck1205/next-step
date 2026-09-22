package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.Syncable
import com.nextstep.app.data.model.ContentScope
import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.ExamType
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.data.sync.EntityMapper
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/** 모든 매퍼: toMap → fromMap 이 원본과 같아야 합니다(dirty 는 수신 시 항상 false). */
class MapperRoundTripTest {
    private fun <T : Syncable> roundTrip(mapper: EntityMapper<T>, entity: T, normalize: (T) -> T): T {
        val back = mapper.fromMap(entity.id, mapper.toMap(entity))
        assertEquals(normalize(entity), normalize(back))
        assertFalse(back.dirty)
        assertFalse(mapper.toMap(entity).containsKey("dirty"))
        return back
    }

    @Test fun subject() = roundTrip(SubjectMapper, Fixtures.subject("s", "수학", 120).copy(teacher = "김", orderIndex = 3, deleted = true)) { it.copy(dirty = false) }.let {}
    @Test fun topic() = roundTrip(TopicMapper, Fixtures.topic("s", "단원", 2, covered = true, status = TopicStatus.MASTERED, confidence = 80)) { it.copy(dirty = false) }.let {}
    @Test fun task() = roundTrip(TaskMapper, Fixtures.task("숙제", LocalDate.of(2026, 9, 1), "s", done = true, type = TaskType.REVIEW, by = "MENTOR").copy(topicId = "t", note = "n")) { it.copy(dirty = false) }.let {}
    @Test fun event() = roundTrip(EventMapper, Fixtures.event("학원", LocalDate.of(2026, 9, 1), LocalTime.of(9, 0), LocalTime.of(10, 30), EventType.ACADEMY, "s", weekly = true).copy(location = "역삼", memo = "m")) { it.copy(dirty = false) }.let {}
    @Test fun grade() = roundTrip(GradeMapper, Fixtures.grade("s", 87.5, 10, classAvg = 70.25).copy(examType = ExamType.MOCK, maxScore = 90.0, memo = "m")) { it.copy(dirty = false) }.let {}
    @Test fun session() = roundTrip(StudySessionMapper, Fixtures.session("s", LocalDate.of(2026, 9, 1), LocalTime.of(20, 0), 45).copy(note = "n", fromTimer = true)) { it.copy(dirty = false) }.let {}
    @Test fun note() = roundTrip(NoteMapper, Fixtures.note("힘내", Role.MENTOR, "쌤")) { it.copy(dirty = false) }.let {}
    @Test fun member() = roundTrip(MemberMapper, Fixtures.member(Role.PARENT, "엄마", subjectIds = "a,b", mentorEnabled = true).copy(title = "t")) { it.copy(dirty = false) }.let {}
    @Test fun roadmap() = roundTrip(RoadmapItemMapper, Fixtures.roadmap("개념", "s", RoadmapStatus.IN_PROGRESS, LocalDate.of(2026, 10, 1), contentId = "c").copy(description = "d", resource = "r", orderIndex = 4, createdByName = "쌤", createdByRole = "MENTOR")) { it.copy(dirty = false) }.let {}

    @Test
    fun contentFamilyMapperRoundTripsAndCatalogForcesGlobalScope() {
        val c = Fixtures.content("영상", "수학", ContentType.EXAM_PREP, "a, b").copy(gradeLevel = GradeLevel.HIGH, ratingSum = 9, ratingCount = 2, durationMinutes = 12, summary = "s", channel = "ch")
        roundTrip(ContentMapper.Family, c) { it.copy(dirty = false) }
        val catalog = ContentMapper.Catalog.fromMap(c.id, ContentMapper.Family.toMap(c))
        assertEquals(ContentScope.GLOBAL, catalog.scope)
        assertEquals("global", catalog.familyId)
        assertEquals("catalog", ContentMapper.Catalog.collection)
    }

    @Test
    fun missingOrWrongTypedFieldsFallBackToDefaults() {
        val topic = TopicMapper.fromMap("x", mapOf("title" to 123, "status" to "NOPE", "orderIndex" to "two"))
        assertEquals("", topic.title); assertEquals(TopicStatus.NOT_STARTED, topic.status); assertEquals(0, topic.orderIndex)
        val roadmap = RoadmapItemMapper.fromMap("y", emptyMap())
        assertNull(roadmap.targetDate); assertNull(roadmap.contentId); assertEquals(RoadmapStatus.PLANNED, roadmap.status)
        val grade = GradeMapper.fromMap("z", mapOf("score" to 50L))
        assertEquals(50.0, grade.score, 0.0); assertEquals(100.0, grade.maxScore, 0.0); assertNull(grade.classAverage)
    }
}
