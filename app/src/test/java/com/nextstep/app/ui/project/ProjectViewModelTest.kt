package com.nextstep.app.ui.project

import androidx.lifecycle.SavedStateHandle
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.project.ProjectCatalog
import com.nextstep.app.domain.project.ProjectPace
import com.nextstep.app.domain.project.ProjectPlanner
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeGoalRepository
import com.nextstep.app.fake.FakeProjectRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProjectViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val goals = FakeGoalRepository()
    private val projects = FakeProjectRepository(streams)
    private val today = LocalDate.of(2029, 3, 7)
    private val plan = ProjectCatalog.byId.getValue("english-reader")

    private fun vm(id: String = "en") = ProjectViewModel(SavedStateHandle(mapOf("goalId" to id)), streams, goals, projects, today = { today })

    private fun seed(from: Int) {
        val (goal, steps) = ProjectPlanner.start(plan, from, today, "PARENT")
        streams.goals.value = listOf(goal.copy(id = "en", familyId = Fixtures.FAMILY))
        streams.goalSteps.value = steps.mapIndexed { i, s -> s.copy(id = "s$i", goalId = "en", familyId = Fixtures.FAMILY) }
    }

    @Test
    fun unknownGoalShowsNothing() = runTest {
        val vm = vm("x"); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertTrue(s.loaded); assertNull(s.progress)
        job.cancel()
    }

    @Test
    fun showsCurrentPhaseScheduleAndSkippedSteps() = runTest {
        seed(4)
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        val p = s.progress!!
        assertEquals("p5", p.current!!.key); assertEquals(ProjectPace.ON_TRACK, p.pace); assertEquals(4, s.skipped)
        assertEquals(10, s.slots.size); assertNull(s.slots[0].start); assertEquals(today, s.slots[4].start)
        job.cancel()
    }

    @Test
    fun passingMovesToTheNextPhaseAndTheLastOneFinishesTheProject() = runTest {
        seed(8)
        val vm = vm(); val job = subscribe(vm.state)
        settle(vm.state)
        vm.onEvent(ProjectEvent.PassCheckpoint); settle(vm.state)
        assertEquals(listOf("stepStatus:s8:DONE", "stepStatus:s9:IN_PROGRESS"), goals.calls)
        streams.goalSteps.value = streams.goalSteps.value.map { if (it.id == "s8") it.copy(status = MilestoneStatus.DONE) else it }
        settle(vm.state)
        vm.onEvent(ProjectEvent.PassCheckpoint); settle(vm.state)
        assertEquals(listOf("stepStatus:s9:DONE", "goalStatus:en:${GoalStatus.DONE}"), goals.calls.drop(2))
        job.cancel()
    }

    @Test
    fun routineAndArchive() = runTest {
        seed(0)
        val vm = vm(); val job = subscribe(vm.state)
        val item = settle(vm.state).progress!!.current!!.routine.single()
        vm.onEvent(ProjectEvent.ToggleRoutine(item))
        assertEquals(setOf(item.name), settle(vm.state).progress!!.todayDoneItems)
        vm.onEvent(ProjectEvent.Archive); settle(vm.state)
        assertEquals(listOf("goalStatus:en:ARCHIVED"), goals.calls)
        job.cancel()
    }
}
