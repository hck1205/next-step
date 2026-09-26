package com.nextstep.app.domain.taskboard

import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.stats.ReviewItem
import com.nextstep.app.domain.stats.ReviewReason
import com.nextstep.app.domain.stats.UpcomingExam
import java.time.LocalDate

/**
 * 과목·단원별 할 일 제안. 흩어져 있던 근거(복습 목록 · 멘토 로드맵 · 다가오는 시험)를 한 목록으로 모으고,
 * 이미 할 일에 있는 것(같은 단원·종류, 또는 같은 제목)은 빼며, 급한 순서([SuggestionSource])로 정렬합니다. 순수 함수입니다.
 */
object TaskSuggester {
    /** 로드맵·시험을 제안에 올리는 기간(일). */
    const val HORIZON_DAYS = 14L
    /** 시험 며칠 전을 마감으로 둘지. */
    const val EXAM_LEAD_DAYS = 3L

    fun suggest(
        review: List<ReviewItem>, roadmap: List<RoadmapItemEntity>, exams: List<UpcomingExam>, subjects: List<SubjectEntity>,
        tasks: List<TaskEntity>, today: LocalDate,
    ): List<TaskSuggestion> {
        val names = subjects.associate { it.id to it.name }
        val horizon = today.plusDays(HORIZON_DAYS)
        val fromReview = review.map { item ->
            val preview = item.reason == ReviewReason.NEXT_CLASS
            val type = if (preview) TaskType.PREVIEW else TaskType.REVIEW
            TaskSuggestion(item.subject.id, item.topic.id, "${item.subject.name} ${item.topic.title} ${type.label}", type, sourceOf(item.reason), today)
        }
        val fromRoadmap = roadmap.filter { !it.deleted && it.status != RoadmapStatus.DONE }
            .filter { r -> r.targetDate?.let { LocalDate.ofEpochDay(it) }?.let { !it.isAfter(horizon) } ?: true }
            .map { r ->
                val due = r.targetDate?.let { LocalDate.ofEpochDay(it) }?.takeIf { !it.isBefore(today) } ?: today
                TaskSuggestion(r.subjectId, null, r.title, TaskType.HOMEWORK, SuggestionSource.ROADMAP, due)
            }
        val fromExams = exams.filter { it.subjectId != null && it.subjectId in names && !it.date.isBefore(today) && !it.date.isAfter(horizon) }.map { e ->
            val due = e.date.minusDays(EXAM_LEAD_DAYS).takeIf { !it.isBefore(today) } ?: today
            TaskSuggestion(e.subjectId, null, "${names.getValue(e.subjectId!!)} ${e.title} 범위 복습", TaskType.EXAM_PREP, SuggestionSource.EXAM, due)
        }
        val open = tasks.filter { !it.deleted && !it.done }
        val taken = { s: TaskSuggestion ->
            open.any { t -> (s.topicId != null && t.topicId == s.topicId && t.type == s.type) || t.title.trim() == s.title }
        }
        return (fromExams + fromReview + fromRoadmap).filterNot(taken).distinctBy { it.key }
            .sortedWith(compareBy<TaskSuggestion> { it.source.ordinal }.thenBy { it.due })
    }

    private fun sourceOf(reason: ReviewReason): SuggestionSource = when (reason) {
        ReviewReason.LOW_CONFIDENCE -> SuggestionSource.LOW_CONFIDENCE
        ReviewReason.SCORE_DROP -> SuggestionSource.SCORE_DROP
        ReviewReason.AFTER_CLASS -> SuggestionSource.AFTER_CLASS
        ReviewReason.NEXT_CLASS -> SuggestionSource.NEXT_CLASS
    }
}
