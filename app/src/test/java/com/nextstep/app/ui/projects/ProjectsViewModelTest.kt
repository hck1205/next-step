package com.nextstep.app.ui.projects

import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.project.ProjectCatalog
import com.nextstep.app.domain.project.ProjectCategory
import com.nextstep.app.domain.project.ProjectPlanner
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeProjectRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProjectsViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val projects = FakeProjectRepository(streams)
    private val today = LocalDate.of(2029, 3, 7)

    private fun vm() = ProjectsViewModel(streams, projects, today = { today })

    private fun run(planId: String, from: Int, id: String, status: GoalStatus = GoalStatus.ACTIVE) {
        val (goal, steps) = ProjectPlanner.start(ProjectCatalog.byId.getValue(planId), from, today, "PARENT")
        streams.goals.value = streams.goals.value + goal.copy(id = id, familyId = Fixtures.FAMILY, status = status)
        streams.goalSteps.value = streams.goalSteps.value + steps.map { it.copy(goalId = id) }
    }

    @Test
    fun emptyUntilAProjectStarts() = runTest {
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertTrue(s.loaded); assertTrue(s.all.isEmpty()); assertTrue(s.categories.isEmpty())
        job.cancel()
    }

    @Test
    fun projectsAreGroupedByCategoryAndArchivedOnesLeave() = runTest {
        run("english-reader", 4, "en"); run("piano", 1, "pi"); run("fitness", 0, "fi", status = GoalStatus.ARCHIVED)
        streams.goals.value = streams.goals.value + Fixtures.goal("수학 목표")
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertEquals(listOf("en", "pi"), s.all.map { it.goalId })
        assertEquals(listOf(ProjectCategory.ENGLISH, ProjectCategory.MUSIC), s.categories)
        vm.onEvent(ProjectsEvent.SelectCategory(ProjectCategory.MUSIC)); s = settle(vm.state)
        assertEquals(listOf("pi"), s.shown.map { it.goalId })
        vm.onEvent(ProjectsEvent.SelectCategory(ProjectCategory.CODING)); s = settle(vm.state)
        assertNull(s.filter); assertEquals(2, s.shown.size)
        job.cancel()
    }

    @Test
    fun routineTapLogsTheCurrentPhaseAndTapAgainUndoes() = runTest {
        run("english-reader", 4, "en")
        val vm = vm(); val job = subscribe(vm.state)
        val p = settle(vm.state).all.single()
        val item = p.current!!.routine.first()
        vm.onEvent(ProjectsEvent.ToggleRoutine(p, item))
        assertEquals(item.minutes, settle(vm.state).all.single().weekMinutes)
        assertEquals(listOf("log:en:p5:${item.name}:${item.minutes}:${today.toEpochDay()}"), projects.calls)
        vm.onEvent(ProjectsEvent.ToggleRoutine(p, item))
        assertEquals(0, settle(vm.state).all.single().weekMinutes)
        job.cancel()
    }
}
