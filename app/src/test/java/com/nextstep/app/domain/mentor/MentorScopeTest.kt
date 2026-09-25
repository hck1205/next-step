package com.nextstep.app.domain.mentor

import com.nextstep.app.data.model.Role
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MentorScopeTest {
    private val all = listOf(Fixtures.math, Fixtures.english)

    @Test
    fun noAssignmentMeansAllSubjects() {
        assertEquals(all, MentorScope.of(null, all).subjects)
        assertEquals(all, MentorScope.of(Fixtures.member(Role.MENTOR, "쌤"), all).subjects)
    }

    @Test
    fun assignedSubjectsNarrowOwnAndGeneralItems() {
        val scope = MentorScope.of(Fixtures.member(Role.MENTOR, "쌤", subjectIds = "math"), all)
        assertEquals(listOf(Fixtures.math), scope.subjects)
        val today = LocalDate.now()
        val tasks = listOf(Fixtures.task("m", today, "math"), Fixtures.task("e", today, "eng"), Fixtures.task("g", today, null))
        assertEquals(listOf("m"), scope.own(tasks) { it.subjectId }.map { it.title })
        assertEquals(listOf("m", "g"), scope.ownOrGeneral(tasks) { it.subjectId }.map { it.title })
    }
}
