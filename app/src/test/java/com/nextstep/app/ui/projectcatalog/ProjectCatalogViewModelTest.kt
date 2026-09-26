package com.nextstep.app.ui.projectcatalog

import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.project.ProjectCategory
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeGoalRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProjectCatalogViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val goals = FakeGoalRepository()
    private val today = LocalDate.of(2029, 3, 7)

    private fun vm() = ProjectCatalogViewModel(streams, goals, today = { today })

    @Test
    fun plansFollowTheChildsAgeAndSuggestAStartingPhase() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "아이", id = "kid", birthDate = today.minusMonths(50)))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertTrue(s.plans.isNotEmpty())
        // 만 4세: 지금 시작할 수 있는 것(독서 → 영어 → 수 감각)이 앞, 초등부터인 코딩은 뒤
        assertEquals(listOf("korean-reader", "english-reader", "number-sense"), s.plans.take(3).map { it.id })
        assertEquals(1, s.suggested["english-reader"])
        assertTrue(s.plans.indexOfFirst { it.id == "coding" } > s.plans.indexOfFirst { it.id == "korean-reader" })
        assertEquals("만 4세 2개월", s.ageLabel)
        vm.onEvent(ProjectCatalogEvent.SelectCategory(ProjectCategory.MUSIC))
        assertEquals(listOf("piano"), settle(vm.state).shown.map { it.id })
        job.cancel()
    }

    @Test
    fun startingSavesGoalAndScheduledStepsOnce() = runTest {
        val vm = vm(); val job = subscribe(vm.state)
        settle(vm.state)
        vm.onEvent(ProjectCatalogEvent.Start("english-reader", 4, "PARENT")); settle(vm.state)
        val goal = goals.addedGoals.single()
        assertEquals("project:english-reader", goal.trackId)
        assertEquals(10, goals.addedSteps.size)
        assertEquals(MilestoneStatus.IN_PROGRESS, goals.addedSteps[4].status)
        streams.goals.value = listOf(goal.copy(familyId = Fixtures.FAMILY))
        assertTrue("english-reader" in settle(vm.state).started)
        vm.onEvent(ProjectCatalogEvent.Start("english-reader", 0, "PARENT")); settle(vm.state)
        assertEquals(1, goals.addedGoals.size)
        vm.onEvent(ProjectCatalogEvent.Start("nope", 0, "PARENT")); settle(vm.state)
        assertFalse(goals.addedGoals.size > 1)
        job.cancel()
    }
}
