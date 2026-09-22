package com.nextstep.app.ui.home

import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.prefs.RunningTimer
import com.nextstep.app.domain.content.ContentRecommendation
import com.nextstep.app.domain.planner.StudyPlan
import com.nextstep.app.domain.stats.EventOccurrence
import com.nextstep.app.domain.stats.SubjectProgress
import com.nextstep.app.domain.stats.UpcomingExam

data class HomeUiState(
    val displayName: String = "",
    val subjects: List<SubjectEntity> = emptyList(),
    val todayEvents: List<EventOccurrence> = emptyList(),
    val pendingTasks: List<TaskEntity> = emptyList(),
    val todayMinutes: Int = 0,
    val weekMinutes: Int = 0,
    val weekGoalMinutes: Int = 0,
    val runningTimer: RunningTimer? = null,
    val progress: List<SubjectProgress> = emptyList(),
    val nextExam: UpcomingExam? = null,
    val loaded: Boolean = false,
    val roadmap: List<RoadmapItemEntity> = emptyList(),
    val events: List<com.nextstep.app.data.local.entity.EventEntity> = emptyList(),
    val lastPlan: StudyPlan? = null,
    val recommendations: List<ContentRecommendation> = emptyList(),
) {
    /** 진행 중이거나 목표일이 가까운 로드맵 항목. */
    val roadmapFocus: List<RoadmapItemEntity> get() = roadmap.filter { it.status != RoadmapStatus.DONE }
        .sortedWith(compareBy<RoadmapItemEntity> { it.status != RoadmapStatus.IN_PROGRESS }.thenBy { it.targetDate ?: Long.MAX_VALUE }).take(3)
    /** 지금 배우는 과목: 학급 진도가 시작됐고 아직 끝나지 않은 과목. */
    val activeSubjects: List<SubjectProgress> get() = progress.filter { it.classCovered > 0 && it.classCovered < it.total }
    val previewQueue: List<Pair<SubjectEntity, TopicEntity>> get() = progress.flatMap { p -> p.previewQueue.take(1).map { p.subject to it } }
    val reviewQueue: List<Pair<SubjectEntity, TopicEntity>> get() = progress.flatMap { p -> p.reviewQueue.take(2).map { p.subject to it } }
}
