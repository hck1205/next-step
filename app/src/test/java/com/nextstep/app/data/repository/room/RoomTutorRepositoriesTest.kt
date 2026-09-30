package com.nextstep.app.data.repository.room

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.lesson.LessonStatus
import com.nextstep.app.domain.report.ReportKind
import com.nextstep.app.domain.roadmap.TemplateItem
import com.nextstep.app.fake.dao.FakeLessonDao
import com.nextstep.app.fake.dao.FakeReportLogDao
import com.nextstep.app.fake.dao.FakeRoadmapTemplateDao
import com.nextstep.app.fake.dao.FakeSubjectDao
import com.nextstep.app.fake.dao.FakeTaskDao
import com.nextstep.app.testing.FakeFamilyScope
import com.nextstep.app.testing.FakeTimeSource
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.testing.RecordingSyncManager
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomTutorRepositoriesTest {
    @Test
    fun reportLogRecordsWhoSentWhat() = runTest {
        val dao = FakeReportLogDao(); val sync = RecordingSyncManager()
        val repo = RoomReportLogRepository(dao, FakeFamilyScope(Role.MENTOR), sync, FakeTimeSource(7L))
        repo.record(ReportKind.MONTH, "지우 수학 월간 수업 리포트 · 3월", "김쌤")
        val log = repo.logs.first().single()
        assertEquals("MONTH", log.kind); assertEquals("me", log.sentById); assertEquals("김쌤", log.sentByName); assertEquals(Fixtures.FAMILY, log.familyId)
        assertTrue(sync.pushRequests >= 1)
    }

    @Test
    fun lessonMarkKeepsOneRowPerMentorPerDay() = runTest {
        val dao = FakeLessonDao(); val sync = RecordingSyncManager()
        val repo = RoomLessonRepository(dao, FakeFamilyScope(Role.MENTOR), sync, FakeTimeSource(5L))
        val day = LocalDate.of(2029, 3, 6)
        repo.mark(day, LessonStatus.DONE)
        repo.mark(day, LessonStatus.MAKEUP, " 감기 ")
        val row = repo.lessons.first().single()
        assertEquals("MAKEUP", row.status); assertEquals("감기", row.note); assertEquals("me", row.mentorId); assertEquals(day.toEpochDay(), row.date)
        repo.mark(day, null)
        assertTrue(repo.lessons.first().isEmpty()); assertTrue(dao.all.single().deleted) // 소프트 삭제
        assertTrue(sync.pushRequests >= 3)
    }

    @Test
    fun templatesStayOnTheDevice() = runTest {
        val dao = FakeRoadmapTemplateDao()
        val repo = RoomRoadmapTemplateRepository(dao, FakeTimeSource(3L))
        repo.save(" ", "수학", listOf(TemplateItem("약분"))); repo.save("초5 수학", "수학", emptyList())
        assertTrue(dao.all.isEmpty())
        repo.save(" 초5 수학 ", "수학", listOf(TemplateItem("약분"), TemplateItem("통분", dayOffset = 7)))
        val t = repo.templates.first().single()
        assertEquals("초5 수학", t.name); assertEquals(2, t.count); assertEquals("수학", t.subjectName)
        repo.delete(t.id); assertTrue(dao.all.isEmpty())
    }

    @Test
    fun bulkAssignMatchesSubjectsByNameInEachFamily() = runTest {
        val tasks = FakeTaskDao(); val subjects = FakeSubjectDao()
        subjects.seed(Fixtures.math.copy(id = "m-b", familyId = "famB"))
        val sync = RecordingSyncManager()
        val repo = RoomBulkTaskRepository(tasks, subjects, FakeFamilyScope(Role.MENTOR), sync, FakeTimeSource(9L))
        val draft = Fixtures.task("약분 20문제", LocalDate.of(2029, 3, 9), "math", by = "MENTOR")
        assertEquals(2, repo.assign(draft, listOf("famB", "famC", "famB"), "수학"))
        val b = tasks.all.single { it.familyId == "famB" }; val c = tasks.all.single { it.familyId == "famC" }
        assertEquals("m-b", b.subjectId); assertNull(c.subjectId) // 같은 이름 과목이 없으면 과목 없이
        assertTrue(b.id != draft.id && b.dirty); assertEquals("MENTOR", c.createdByRole); assertEquals(1, sync.pushRequests)
        assertEquals(0, repo.assign(draft.copy(title = " "), listOf("famB"), null))
    }
}
