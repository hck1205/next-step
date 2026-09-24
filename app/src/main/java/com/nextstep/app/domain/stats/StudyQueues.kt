package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.TopicStatus

/** 화면이 "지금 할 것"으로 고르는 목록들. 상태를 만들 때 한 번 계산합니다(가이드 6장). */
object StudyQueues {
    /** 과목마다 첫 화면에 올릴 예습·복습 단원 수. */
    const val PREVIEW_PER_SUBJECT = 1
    const val REVIEW_PER_SUBJECT = 2

    /** 학급 진도가 시작됐고 아직 끝나지 않은 과목. */
    fun activeSubjects(progress: List<SubjectProgress>): List<SubjectProgress> = progress.filter { it.classCovered in 1 until it.total }

    fun previewQueue(progress: List<SubjectProgress>, perSubject: Int = PREVIEW_PER_SUBJECT): List<Pair<SubjectEntity, TopicEntity>> =
        progress.flatMap { p -> p.previewQueue.take(perSubject).map { p.subject to it } }

    fun reviewQueue(progress: List<SubjectProgress>, perSubject: Int = REVIEW_PER_SUBJECT): List<Pair<SubjectEntity, TopicEntity>> =
        progress.flatMap { p -> p.reviewQueue.take(perSubject).map { p.subject to it } }

    /** 진행 중인 것이 먼저, 그다음 목표일이 가까운 순서로 끝나지 않은 로드맵 [limit]개. */
    fun roadmapFocus(items: List<RoadmapItemEntity>, limit: Int): List<RoadmapItemEntity> = items.filter { it.status != RoadmapStatus.DONE }
        .sortedWith(compareBy<RoadmapItemEntity> { it.status != RoadmapStatus.IN_PROGRESS }.thenBy { it.targetDate ?: Long.MAX_VALUE })
        .take(limit)

    /** 한 과목의 학급 진도 위치(마지막으로 수업한 단원 순서). 없으면 -1. */
    fun classIndex(topics: List<TopicEntity>): Int = topics.filter { it.classCovered }.maxOfOrNull { it.orderIndex } ?: -1

    fun previewTopics(topics: List<TopicEntity>): List<TopicEntity> = topics.filter { !it.classCovered && it.status.order < TopicStatus.PREVIEWED.order }

    fun reviewTopics(topics: List<TopicEntity>): List<TopicEntity> = topics.filter { it.classCovered && it.status.order < TopicStatus.REVIEWED.order }
}
