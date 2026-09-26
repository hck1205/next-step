package com.nextstep.app.ui.todo

import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.domain.selfdirection.SelfDirectionStage
import com.nextstep.app.domain.taskboard.SuggestionSource
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeTaskRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TodoViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.STUDENT)
    private val tasks = FakeTaskRepository(streams)
    private val today = LocalDate.of(2029, 5, 10)

    private fun vm() = TodoViewModel(streams, tasks, today = { today })

    @Test
    fun boardGroupsBySubjectWithSuggestionsAndFilters() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "지우", id = "kid", gradeYear = 3))
        streams.subjects.value = listOf(Fixtures.math, Fixtures.english)
        streams.topics.value = listOf(Fixtures.topic("math", "분수", 1, covered = true, confidence = 30))
        streams.tasks.value = listOf(Fixtures.task("밀린 단어", today.minusDays(1), "eng"), Fixtures.task("생활", today.plusDays(1)))
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertEquals(listOf("eng", "math", null), s.lanes.map { it.subject?.id })
        assertEquals(SuggestionSource.LOW_CONFIDENCE, s.lanes[1].suggestions.single().source)
        assertEquals(1, s.overdueCount); assertEquals(1, s.suggestionCount); assertEquals(SelfDirectionStage.PLAN_TOGETHER, s.stage)
        vm.onEvent(TodoEvent.SetFilter(TodoFilter.OVERDUE)); s = settle(vm.state)
        assertEquals(listOf("eng"), s.shown.map { it.subject?.id })
        job.cancel()
    }

    @Test
    fun acceptingASuggestionMakesATaskOptionallyInsideAGoal() = runTest {
        streams.subjects.value = listOf(Fixtures.math)
        streams.topics.value = listOf(Fixtures.topic("math", "분수", 1, covered = true, confidence = 30))
        streams.goals.value = listOf(Fixtures.goal("수학 자신감", trackId = GoalTree.TRACK, id = "g"))
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        val sug = s.lanes.single().suggestions.single()
        vm.onEvent(TodoEvent.Accept(sug, "g", "PARENT")); s = settle(vm.state)
        val t = tasks.saved.single()
        assertEquals("수학 분수 복습", t.title); assertEquals(TaskType.REVIEW, t.type); assertEquals("g", t.goalId); assertEquals("PARENT", t.createdByRole)
        assertTrue(t.note.startsWith("수학 자신감"))
        assertTrue(s.lanes.single().suggestions.isEmpty()) // 할 일이 되면 추천에서 빠짐
        vm.onEvent(TodoEvent.Toggle(t.id, true)); settle(vm.state)
        assertTrue(streams.tasks.value.single().done)
        job.cancel()
    }
}
