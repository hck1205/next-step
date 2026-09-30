package com.nextstep.app.domain.stats

import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewPlannerTest {
    @Test
    fun ordersLowConfidenceThenDroppedSubjectsThenAfterClassThenOnePreviewPerSubject() {
        val topics = listOf(
            Fixtures.topic("math", "m0", 0, covered = true, status = TopicStatus.REVIEWED, confidence = 40),
            Fixtures.topic("math", "m1", 1, covered = true, status = TopicStatus.IN_CLASS),
            Fixtures.topic("math", "m2", 2, covered = true, status = TopicStatus.IN_CLASS),
            Fixtures.topic("math", "m3", 3),
            Fixtures.topic("math", "m4", 4),
            Fixtures.topic("math", "숙달", 5, covered = true, status = TopicStatus.MASTERED, confidence = 30),
            Fixtures.topic("math", "지움", 6, covered = true).copy(deleted = true),
            Fixtures.topic("eng", "e0", 0, covered = true, status = TopicStatus.IN_CLASS),
            Fixtures.topic("eng", "e1", 1, status = TopicStatus.PREVIEWED),
            Fixtures.topic("eng", "e2", 2),
        )
        val grades = listOf(Fixtures.grade("eng", 90.0, 1), Fixtures.grade("eng", 70.0, 2), Fixtures.grade("math", 60.0, 1), Fixtures.grade("math", 80.0, 2))
        val plan = ReviewPlanner.plan(topics, listOf(Fixtures.math, Fixtures.english), grades)
        assertEquals(
            listOf(
                "m0" to ReviewReason.LOW_CONFIDENCE, "e0" to ReviewReason.SCORE_DROP,
                "m2" to ReviewReason.AFTER_CLASS, "m1" to ReviewReason.AFTER_CLASS,
                "m3" to ReviewReason.NEXT_CLASS, "e2" to ReviewReason.NEXT_CLASS,
            ),
            plan.map { it.topic.title to it.reason },
        )
    }
}
