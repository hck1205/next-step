package com.nextstep.app.domain.mentor

import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class AssignmentStatsTest {
    private val today = LocalDate.of(2029, 10, 10)

    @Test
    fun countsOnlyMentorAssignmentsAndSplitsOverdueSoonAndDone() {
        val tasks = listOf(
            Fixtures.task("밀림", today.minusDays(3), "math", by = "MENTOR"),
            Fixtures.task("곧", today.plusDays(2), "math", by = "MENTOR"),
            Fixtures.task("어제 끝", today.minusDays(1), "eng", done = true, by = "MENTOR"),
            Fixtures.task("옛날 끝", today.minusDays(5), "math", done = true, by = "MENTOR"),
            Fixtures.task("나중", today.plusDays(20), null, by = "MENTOR"),
            Fixtures.task("내 할 일", today, "math"),
            Fixtures.task("지움", today, "math", by = "MENTOR").copy(deleted = true),
        )
        val r = AssignmentStats.report(tasks, listOf(Fixtures.math, Fixtures.english), today)
        assertEquals(5, r.total); assertEquals(2, r.done); assertEquals(40, r.percent)
        assertEquals(listOf("밀림"), r.overdue.map { it.title })
        assertEquals(listOf("곧"), r.dueSoon.map { it.title })
        assertEquals(listOf("어제 끝", "옛날 끝"), r.recentDone.map { it.title })
        assertEquals(listOf("수학" to "1/3", "영어" to "1/1", null to "0/1"), r.bySubject.map { it.subject?.name to "${it.done}/${it.total}" })
        assertEquals("밀린 과제 1개부터 같이 챙겨요.", r.line)
    }

    @Test
    fun lineWhenNothingIsDueOrAssigned() {
        assertEquals("아직 낸 과제가 없어요. + 버튼으로 과제를 내면 여기서 챙겨요.", AssignmentStats.report(emptyList(), emptyList(), today).line)
        val done = listOf(Fixtures.task("끝", today.minusDays(1), "math", done = true, by = "MENTOR"))
        assertEquals("낸 과제 1개 중 1개 끝냈어요.", AssignmentStats.report(done, listOf(Fixtures.math), today).line)
    }
}
