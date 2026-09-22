package com.nextstep.app.ui.progress

import androidx.lifecycle.SavedStateHandle
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeSubjectRepository
import com.nextstep.app.fake.FakeTaskRepository
import com.nextstep.app.fake.FakeTopicRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProgressViewModelsTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams()
    private val subjects = FakeSubjectRepository()
    private val topics = FakeTopicRepository()
    private val tasks = FakeTaskRepository()

    @Test
    fun progressStateAndSubjectEvents() = runTest {
        streams.subjects.value = listOf(Fixtures.math)
        streams.topics.value = Fixtures.topics("math", 5, covered = 3, reviewed = 1)
        val vm = ProgressViewModel(streams, subjects); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(2, s.progress.single().reviewQueue.size) // queueSize = 2
        vm.onEvent(ProgressEvent.AddSubject("과학", 0xFF000000, 90, "박쌤"))
        vm.onEvent(ProgressEvent.UpdateSubject(Fixtures.math.copy(name = "수학2")))
        vm.onEvent(ProgressEvent.DeleteSubject("math"))
        settle(vm.state)
        assertEquals(listOf("과학", "수학2"), subjects.saved.map { it.name })
        assertEquals(1, subjects.saved.first().orderIndex)
        assertEquals("박쌤", subjects.saved.first().teacher)
        assertEquals(listOf("math"), subjects.deleted)
        job.cancel()
    }

    @Test
    fun subjectDetailDerivesQueuesAndClassIndex() = runTest {
        subjects.subjects.value = listOf(Fixtures.math)
        topics.topics.value = Fixtures.topics("math", 4, covered = 2, reviewed = 1)
        val vm = SubjectDetailViewModel(SavedStateHandle(mapOf("subjectId" to "math")), streams, subjects, topics, tasks)
        val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals("수학", s.subject!!.name)
        assertEquals(1, s.classIndex)
        assertEquals(listOf("단원 1"), s.reviewQueue.map { it.title })
        assertEquals(listOf("단원 2", "단원 3"), s.previewQueue.map { it.title })
        job.cancel()
    }

    @Test
    fun subjectDetailEventsMapToRepositoryCalls() = runTest {
        subjects.subjects.value = listOf(Fixtures.math)
        val vm = SubjectDetailViewModel(SavedStateHandle(mapOf("subjectId" to "math")), streams, subjects, topics, tasks)
        val job = subscribe(vm.state); settle(vm.state)
        val topic = Fixtures.topic("math", "일차방정식", 0)
        vm.onEvent(SubjectDetailEvent.AddTopics("a, b\nc\n\n"))
        vm.onEvent(SubjectDetailEvent.SetStatus(topic, TopicStatus.PREVIEWED))
        vm.onEvent(SubjectDetailEvent.SetConfidence(topic, 70))
        vm.onEvent(SubjectDetailEvent.Rename(topic, "새 이름"))
        vm.onEvent(SubjectDetailEvent.SetClassProgress(2))
        vm.onEvent(SubjectDetailEvent.Delete(topic))
        vm.onEvent(SubjectDetailEvent.AddTask(topic, TaskType.REVIEW, "MENTOR"))
        vm.onEvent(SubjectDetailEvent.UpdateSubject(Fixtures.math.copy(weeklyGoalMinutes = 60)))
        settle(vm.state)
        assertEquals("add:math:a|b|c", topics.calls[0])
        assertEquals("status:t-math-0:PREVIEWED", topics.calls[1])
        assertEquals("update:t-math-0", topics.calls[2]) // confidence
        assertEquals("update:t-math-0", topics.calls[3]) // rename
        assertEquals("class:math:2", topics.calls[4])
        assertEquals("delete:t-math-0", topics.calls[5])
        val task = tasks.saved.single()
        assertEquals("수학 일차방정식 복습", task.title); assertEquals("MENTOR", task.createdByRole)
        assertEquals(60, subjects.saved.single().weeklyGoalMinutes)
        job.cancel()
    }

    @Test
    fun subjectDetailWithoutSubjectSkipsTaskCreation() = runTest {
        val vm = SubjectDetailViewModel(SavedStateHandle(mapOf("subjectId" to "ghost")), streams, subjects, topics, tasks)
        val job = subscribe(vm.state); settle(vm.state)
        assertNull(vm.state.value.subject)
        vm.onEvent(SubjectDetailEvent.AddTask(Fixtures.topic("ghost", "x", 0), TaskType.REVIEW, "STUDENT"))
        settle(vm.state)
        assertEquals(0, tasks.saved.size)
        job.cancel()
    }
}
