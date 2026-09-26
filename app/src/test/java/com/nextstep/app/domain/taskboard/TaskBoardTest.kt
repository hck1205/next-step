package com.nextstep.app.domain.taskboard

import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.stats.ReviewItem
import com.nextstep.app.domain.stats.ReviewReason
import com.nextstep.app.domain.stats.UpcomingExam
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class TaskBoardTest {
    private val today = LocalDate.of(2029, 5, 10)
    private val subjects = listOf(Fixtures.math, Fixtures.english)
    private val t1 = Fixtures.topic("math", "분수", 1, covered = true, confidence = 30)
    private val t2 = Fixtures.topic("math", "소수", 2)

    @Test
    fun suggestionsMergeReviewRoadmapAndExamsInUrgencyOrderWithoutDuplicates() {
        val review = listOf(ReviewItem(Fixtures.math, t1, ReviewReason.LOW_CONFIDENCE), ReviewItem(Fixtures.math, t2, ReviewReason.NEXT_CLASS))
        val roadmap = listOf(
            Fixtures.roadmap("영어 리더스 3권", "eng", target = today.plusDays(5)), Fixtures.roadmap("먼 로드맵", "eng", target = today.plusDays(40)),
            Fixtures.roadmap("끝낸 것", "eng", status = RoadmapStatus.DONE),
        )
        val exams = listOf(UpcomingExam("단원평가", "math", today.plusDays(7)), UpcomingExam("먼 시험", "math", today.plusDays(30)))
        val tasks = listOf(Fixtures.task("이미 있음", today, "math").copy(topicId = t2.id, type = TaskType.PREVIEW))
        val s = TaskSuggester.suggest(review, roadmap, exams, subjects, tasks, today)
        assertEquals(listOf(SuggestionSource.EXAM, SuggestionSource.LOW_CONFIDENCE, SuggestionSource.ROADMAP), s.map { it.source })
        assertEquals("수학 단원평가 범위 복습", s[0].title); assertEquals(today.plusDays(4), s[0].due); assertEquals(TaskType.EXAM_PREP, s[0].type)
        assertEquals("수학 분수 복습", s[1].title); assertEquals(t1.id, s[1].topicId)
        assertEquals(today.plusDays(5), s[2].due)
    }

    @Test
    fun lanesGroupBySubjectWithOverdueFirstAndOtherLast() {
        val zone = ZoneOffset.UTC
        val done = Fixtures.task("끝", today, "eng", done = true).copy(doneAt = today.atStartOfDay(zone).toInstant().toEpochMilli())
        val tasks = listOf(
            Fixtures.task("밀림", today.minusDays(1), "eng"), Fixtures.task("오늘", today, "math"), Fixtures.task("다음", today.plusDays(2), "math"),
            Fixtures.task("생활", today, null), done, Fixtures.task("없는 과목", today, "gone"),
        )
        val sug = listOf(TaskSuggestion("math", null, "수학 제안", TaskType.REVIEW, SuggestionSource.AFTER_CLASS, today))
        val lanes = TaskBoard.lanes(tasks, subjects, sug, today, zone)
        assertEquals(listOf("eng", "math", null), lanes.map { it.subject?.id })
        assertEquals(1, lanes[0].overdue.size); assertEquals(1, lanes[0].doneThisWeek); assertEquals(0.5f, lanes[0].recentRate!!, 0.001f)
        assertEquals(listOf("오늘"), lanes[1].today.map { it.title }); assertEquals(listOf("다음"), lanes[1].upcoming.map { it.title })
        assertEquals(1, lanes[1].suggestions.size)
        assertEquals(setOf("생활", "없는 과목"), lanes[2].today.map { it.title }.toSet())
        assertTrue(TaskBoard.lanes(emptyList(), subjects, emptyList(), today, zone).isEmpty())
        assertNull(TaskBoard.lanes(listOf(Fixtures.task("다음", today.plusDays(2), "math")), subjects, emptyList(), today, zone).single().recentRate)
    }
}
