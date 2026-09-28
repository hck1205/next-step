package com.nextstep.app.data.repository

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.project.ProjectCatalog
import com.nextstep.app.domain.project.ProjectPlanner
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.testing.Fixtures
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectScopedStreamsTest {
    private val today = LocalDate.of(2029, 3, 7)

    /** 프로필의 memberId 는 "me". 학생이 만든 것 · 내가 준 것 · 다른 멘토가 준 것 · 일반 목표. */
    private fun family(role: Role) = FakeFamilyDataStreams(role = role).apply {
        val starts = listOf("self" to ("STUDENT" to "kid"), "mine" to (role.name to "me"), "other" to ("MENTOR" to "m9"))
        starts.forEach { (id, by) ->
            val (goal, steps) = ProjectPlanner.start(ProjectCatalog.byId.getValue("piano"), 0, today, by.first)
            goals.value = goals.value + goal.copy(id = id, familyId = Fixtures.FAMILY, createdById = by.second)
            goalSteps.value = goalSteps.value + steps.map { it.copy(goalId = id) }
            projectLogs.value = projectLogs.value + Fixtures.projectLog(id, "p1", "건반", 10, today)
        }
        goals.value = goals.value + Fixtures.goal("수학 목표")
    }

    @Test
    fun creatorSeesOnlyWhatTheyGaveWithItsStepsAndLogs() = runTest {
        val s = ProjectScopedStreams(family(Role.MENTOR))
        assertEquals(listOf("mine", "g-수학 목표"), s.goals.first().map { it.id })
        assertEquals(setOf("mine"), s.goalSteps.first().map { it.goalId }.toSet())
        assertEquals(listOf("mine"), s.projectLogs.first().map { it.goalId })
        assertEquals(listOf("mine", "g-수학 목표"), ProjectScopedStreams(family(Role.PARENT)).goals.first().map { it.id })
    }

    @Test
    fun studentSeesEveryProject() = runTest {
        val s = ProjectScopedStreams(family(Role.STUDENT))
        assertEquals(4, s.goals.first().size)
        assertEquals(3, s.projectLogs.first().size)
    }
}
