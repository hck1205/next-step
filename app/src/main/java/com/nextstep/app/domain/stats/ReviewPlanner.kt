package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TopicStatus

/**
 * 지금 다시 볼 단원을 과목을 가로질러 급한 순서로 고릅니다.
 * 이해도 낮음 → 점수가 내려간 과목의 복습 전 단원 → 수업 뒤 복습 전 → 다음 수업 예습(과목마다 하나).
 */
object ReviewPlanner {
    /** 이해도(%)가 이 값 미만이면 복습 전·후와 상관없이 다시 봅니다. 0 은 아직 안 적음. */
    const val LOW_CONFIDENCE = 60

    fun plan(topics: List<TopicEntity>, subjects: List<SubjectEntity>, grades: List<GradeEntity>): List<ReviewItem> {
        val dropped = StudyStats.subjectScores(grades.filter { !it.deleted }, subjects).filter { (it.trend ?: 0.0) < 0 }.map { it.subject.id }.toSet()
        return subjects.flatMap { subject ->
            val list = topics.filter { !it.deleted && it.subjectId == subject.id }.sortedBy { it.orderIndex }
            val low = list.filter { it.confidence in 1 until LOW_CONFIDENCE && it.status != TopicStatus.MASTERED }
            val after = (list - low.toSet()).filter { it.classCovered && it.status.order < TopicStatus.REVIEWED.order }
                .sortedByDescending { it.orderIndex }
            val next = list.firstOrNull { !it.classCovered && it.status.order < TopicStatus.PREVIEWED.order && it !in low }
            low.map { ReviewItem(subject, it, ReviewReason.LOW_CONFIDENCE) } +
                after.map { ReviewItem(subject, it, if (subject.id in dropped) ReviewReason.SCORE_DROP else ReviewReason.AFTER_CLASS) } +
                listOfNotNull(next?.let { ReviewItem(subject, it, ReviewReason.NEXT_CLASS) })
        }.sortedWith(compareBy<ReviewItem> { it.reason.ordinal }.thenBy { it.topic.confidence.takeIf { c -> c > 0 } ?: Int.MAX_VALUE })
    }
}
