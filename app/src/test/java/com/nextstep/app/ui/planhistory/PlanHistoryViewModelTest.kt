package com.nextstep.app.ui.planhistory

import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class PlanHistoryViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val today = LocalDate.of(2029, 5, 10)

    @Test
    fun ratesAchievedGoalsAndTimeline() = runTest {
        streams.subjects.value = listOf(Fixtures.math)
        streams.goals.value = listOf(
            Fixtures.goal("큰 목표", trackId = GoalTree.TRACK, id = "big"),
            Fixtures.goal("작은 목표", trackId = GoalTree.TRACK, id = "s", status = GoalStatus.DONE).copy(leadsTo = "big", doneAt = 5L),
        )
        streams.tasks.value = listOf(
            Fixtures.task("a", today, "math", done = true, by = "PARENT").copy(doneAt = 7L, goalId = "s"),
            Fixtures.task("b", today.minusDays(1), by = "STUDENT"),
        )
        val vm = PlanHistoryViewModel(streams, today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(0.5f, s.recentRate!!, 0.001f); assertEquals(8, s.weeks.size)
        assertEquals(listOf("스스로", "학부모가"), s.byAssigner.map { it.label })
        assertEquals("→ 큰 목표 100%", s.achieved.single().second)
        assertEquals(4, s.timeline.size) // 목표 시작 2 + 목표 달성 1 + 할 일 끝 1
        job.cancel()
    }
}
