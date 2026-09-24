package com.nextstep.app.domain.stats

import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StudyQueuesTest {
    private val math = StudyStats.subjectProgress(Fixtures.topics("math", 5, covered = 3, reviewed = 0), listOf(Fixtures.math))
    private val eng = StudyStats.subjectProgress(Fixtures.topics("eng", 2, covered = 2, reviewed = 2), listOf(Fixtures.english))

    @Test
    fun activeSubjectsAreStartedButUnfinished() {
        assertEquals(listOf("수학"), StudyQueues.activeSubjects(math + eng).map { it.subject.name })
    }

    @Test
    fun queuesTakeAFewPerSubject() {
        assertEquals(StudyQueues.REVIEW_PER_SUBJECT, StudyQueues.reviewQueue(math).size)
        assertEquals(listOf("단원 3"), StudyQueues.previewQueue(math).map { it.second.title })
        assertEquals(3, StudyQueues.reviewQueue(math, perSubject = 5).size)
    }

    @Test
    fun roadmapFocusPutsInProgressFirstThenSoonest() {
        val d = LocalDate.of(2026, 4, 1)
        val items = listOf(
            Fixtures.roadmap("done", status = RoadmapStatus.DONE), Fixtures.roadmap("late", target = d.plusDays(30)),
            Fixtures.roadmap("soon", target = d.plusDays(3)), Fixtures.roadmap("doing", status = RoadmapStatus.IN_PROGRESS, target = d.plusDays(90)),
            Fixtures.roadmap("none"),
        )
        assertEquals(listOf("doing", "soon", "late"), StudyQueues.roadmapFocus(items, 3).map { it.title })
    }

    @Test
    fun topicQueuesFollowClassProgressAndMyStatus() {
        val topics = listOf(
            Fixtures.topic("m", "a", 0, covered = true, status = TopicStatus.REVIEWED),
            Fixtures.topic("m", "b", 1, covered = true, status = TopicStatus.IN_CLASS),
            Fixtures.topic("m", "c", 2), Fixtures.topic("m", "d", 3, status = TopicStatus.PREVIEWED),
        )
        assertEquals(1, StudyQueues.classIndex(topics)); assertEquals(-1, StudyQueues.classIndex(emptyList()))
        assertEquals(listOf("b"), StudyQueues.reviewTopics(topics).map { it.title })
        assertEquals(listOf("c"), StudyQueues.previewTopics(topics).map { it.title })
    }
}
