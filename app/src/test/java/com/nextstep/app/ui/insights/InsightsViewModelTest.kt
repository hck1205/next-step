package com.nextstep.app.ui.insights

import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.insight.InsightAction
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeTaskRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class InsightsViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams()
    private val tasks = FakeTaskRepository()

    @Test
    fun stateBundlesInsightsTalentsAndDistributions() = runTest {
        val today = DateUtils.today()
        streams.subjects.value = listOf(Fixtures.math, Fixtures.english)
        streams.grades.value = listOf(Fixtures.grade("math", 55.0, 1), Fixtures.grade("eng", 95.0, 1))
        streams.sessions.value = (0 until 10).map { Fixtures.session("eng", today.minusDays(it.toLong()), LocalTime.of(7, 0), 30) }
        val vm = InsightsViewModel(streams, tasks); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertTrue(s.insights.any { it.subjectId == "math" })
        assertTrue(s.talents.any { it.title == "꾸준함" })
        assertEquals(14, s.daily14.size); assertEquals(300, s.totalMinutes); assertEquals(300, s.byHour[7])
        assertEquals(2, s.scores.size)
        job.cancel()
    }

    @Test
    fun applyActionCreatesTaskDueTomorrowWithGivenRole() = runTest {
        val vm = InsightsViewModel(streams, tasks); val job = subscribe(vm.state)
        vm.onEvent(InsightsEvent.ApplyAction(InsightAction.CreateTask("수학 복습", "math", "t1", TaskType.REVIEW), "MENTOR"))
        settle(vm.state)
        val t = tasks.saved.single()
        assertEquals("수학 복습", t.title); assertEquals("t1", t.topicId); assertEquals("MENTOR", t.createdByRole)
        assertEquals(DateUtils.today().plusDays(1).toEpochDay(), t.dueDate)
        job.cancel()
    }
}
