package com.nextstep.app.domain.report

import com.nextstep.app.domain.feedback.FeedbackLine
import com.nextstep.app.domain.mentor.AssignmentStats
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LessonReportsTest {
    private val today = LocalDate.of(2029, 3, 7)

    private fun input(tasks: List<com.nextstep.app.data.local.entity.TaskEntity> = emptyList(), minutes: Int = 130, notes: List<FeedbackLine> = emptyList()) = LessonReportInput(
        "지우", "김쌤", listOf(Fixtures.math), today, minutes,
        StudyStats.subjectProgress(Fixtures.topics("math", 8, covered = 4, reviewed = 3), listOf(Fixtures.math)),
        AssignmentStats.report(tasks, listOf(Fixtures.math), today), notes,
    )

    @Test
    fun reportGathersStudyProgressAssignmentsAndNotes() {
        val tasks = listOf(
            Fixtures.task("약분 20문제", today.plusDays(2), "math", by = "MENTOR"),
            Fixtures.task("통분 복습", today.minusDays(1), "math", by = "MENTOR"),
            Fixtures.task("분수 단원평가", today.minusDays(3), "math", done = true, by = "MENTOR"),
            Fixtures.task("스스로 한 것", today, "math"),
        )
        val r = LessonReports.of(input(tasks, notes = listOf(FeedbackLine("공부한 날이 줄었어요 (5일 → 2일)", "", false))))
        assertTrue(r.title.startsWith("지우 수학 수업 리포트")); assertEquals("김쌤 선생님", r.from)
        assertEquals(listOf("이번 주 공부", "진도", "과제", "이번 주 살펴본 것"), r.sections.map { it.label })
        assertEquals(listOf("2시간 10분"), r.sections[0].lines)
        assertEquals(listOf("수학 8단원 중 수업 4 · 복습 3"), r.sections[1].lines)
        assertEquals("끝냄 1 · 남음 1 · 밀림 1", r.sections[2].lines[0]) // 멘토가 낸 과제만
        assertEquals(listOf("통분 복습 — 3/6까지", "약분 20문제 — 3/9까지"), r.sections[2].lines.drop(1))
        val text = LessonReports.text(r, "  분수 개념이 잘 잡혔어요 ")
        assertTrue(text.contains("■ 선생님 한마디\n  분수 개념이 잘 잡혔어요")); assertTrue(text.endsWith("— NextStep에서 보냄"))
        assertFalse(LessonReports.text(r, " ").contains("선생님 한마디"))
    }

    @Test
    fun emptyPartsAreLeftOut() {
        val r = LessonReports.of(input(minutes = 0).copy(progress = emptyList()))
        assertEquals(listOf("이번 주 공부"), r.sections.map { it.label }); assertEquals(listOf("아직 기록이 없어요"), r.sections[0].lines)
    }
}
