package com.nextstep.app.ui.home

import com.nextstep.app.domain.mission.MissionFocus
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.domain.curriculum.TermCurriculum
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.UiDefaults
import java.time.LocalDate
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
import com.nextstep.app.domain.planner.PlanOptions

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
    val events: List<EventEntity> = emptyList(),
    val lastPlan: StudyPlan? = null,
    val recommendations: List<ContentRecommendation> = emptyList(),
    val stage: GrowthStage? = null,
    /** 성장 단계에 맞춘 학습 계획 기본값. */
    val planDefaults: PlanOptions = PlanOptions(),
    /** 여정에서 지금 준비하거나 놓친 항목 (최대 3개). 대학·대학원생 등 본인이 관리하는 경우를 위해 학생 홈에도 보여 줍니다. */
    val journeyNow: List<JourneyItem> = emptyList(),
    val hasBirthDate: Boolean = false,
    val today: LocalDate = DateUtils.today(),
    /** 가장 최근 격려·메모. 학생 홈 상단 카드. */
    val latestNote: NoteEntity? = null,
    /** 이번 학기 교과 커리큘럼과 구간 이름. 학령기 + 생년월일/학년이 있을 때만. */
    val curriculum: TermCurriculum? = null,
    val periodLabel: String? = null,
    /** 날짜 목표(시험·수행평가·입시)의 다음 한 걸음. 가까운 순서로 3개까지. */
    val missionFocus: List<MissionFocus> = emptyList(),
) {
    /** 진행 중이거나 목표일이 가까운 로드맵 항목. */
    val roadmapFocus: List<RoadmapItemEntity> get() = roadmap.filter { it.status != RoadmapStatus.DONE }
        .sortedWith(compareBy<RoadmapItemEntity> { it.status != RoadmapStatus.IN_PROGRESS }.thenBy { it.targetDate ?: Long.MAX_VALUE }).take(UiDefaults.MAX_ROWS)
    /** 지금 배우는 과목: 학급 진도가 시작됐고 아직 끝나지 않은 과목. */
    val activeSubjects: List<SubjectProgress> get() = progress.filter { it.classCovered > 0 && it.classCovered < it.total }
    val previewQueue: List<Pair<SubjectEntity, TopicEntity>> get() = progress.flatMap { p -> p.previewQueue.take(1).map { p.subject to it } }
    val reviewQueue: List<Pair<SubjectEntity, TopicEntity>> get() = progress.flatMap { p -> p.reviewQueue.take(2).map { p.subject to it } }
}
