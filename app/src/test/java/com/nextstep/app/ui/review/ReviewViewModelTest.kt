package com.nextstep.app.ui.review

import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.stats.ReviewReason
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeTaskRepository
import com.nextstep.app.fake.FakeTopicRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ReviewViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.STUDENT)
    private val tasks = FakeTaskRepository()
    private val topics = FakeTopicRepository()
    private val today = LocalDate.of(2029, 10, 10)

    @Test
    fun groupsByReasonAndEventsWriteTasksAndStatus() = runTest {
        streams.subjects.value = listOf(Fixtures.math)
        streams.topics.value = listOf(
            Fixtures.topic("math", "분수", 0, covered = true, status = TopicStatus.IN_CLASS, confidence = 30),
            Fixtures.topic("math", "소수", 1, covered = true, status = TopicStatus.IN_CLASS),
            Fixtures.topic("math", "비율", 2),
        )
        val vm = ReviewViewModel(streams, tasks, topics, today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(listOf(ReviewReason.LOW_CONFIDENCE, ReviewReason.AFTER_CLASS, ReviewReason.NEXT_CLASS), s.sections.map { it.first })
        assertEquals(3, s.total)
        assertEquals(listOf("수학" to 2), s.perSubject)

        val next = s.sections.last().second.single()
        vm.onEvent(ReviewEvent.AddTask(next, "STUDENT"))
        vm.onEvent(ReviewEvent.MarkDone(next))
        vm.onEvent(ReviewEvent.MarkDone(s.sections.first().second.single()))
        settle(vm.state)
        val saved = tasks.saved.single()
        assertEquals("수학 비율 예습", saved.title); assertEquals(TaskType.PREVIEW, saved.type); assertEquals(today.toEpochDay(), saved.dueDate); assertEquals("t-math-2", saved.topicId)
        assertEquals(listOf("status:t-math-2:PREVIEWED", "status:t-math-0:REVIEWED"), topics.calls)
        job.cancel()
    }
}
