package com.nextstep.app.ui.home

import com.nextstep.app.domain.growth.StudentScreen
import com.nextstep.app.domain.growth.YearProfile
import com.nextstep.app.domain.stats.DayMinutes
import com.nextstep.app.domain.growth.StudentHomeSection
import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.domain.mission.MissionFocus
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.domain.curriculum.TermCurriculum
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
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
    /** 이번 학기 교과 커리큘럼과 구간 이름. 학령기 + 생년월일/학년이 있을 때만. */
    val curriculum: TermCurriculum? = null,
    val periodLabel: String? = null,
    /** 날짜 목표(시험·수행평가·입시)의 다음 한 걸음. 가까운 순서로 3개까지. */
    val missionFocus: List<MissionFocus> = emptyList(),
    /** 아래 네 목록은 ViewModel 이 StudyQueues 로 한 번 계산합니다. */
    val roadmapFocus: List<RoadmapItemEntity> = emptyList(),
    val activeSubjects: List<SubjectProgress> = emptyList(),
    val previewQueue: List<Pair<SubjectEntity, TopicEntity>> = emptyList(),
    val reviewQueue: List<Pair<SubjectEntity, TopicEntity>> = emptyList(),
    /** 학년에 맞춘 화면 단계. 어떤 카드를 몇 줄, 어떤 말로 보여 줄지 정합니다. 학생 정보가 없으면 전체 화면. */
    val level: StudentUiLevel = StudentUiLevel.TREE,
    /** 올해 프로필(만 나이·학년별 공부 종류와 양). 없으면 null. */
    val year: YearProfile? = null,
    /** 할 일 줄 수와 카드 순서: 해마다 달라집니다(StudentScreen). */
    val taskRows: Int = StudentUiLevel.TREE.taskRows,
    val homeOrder: List<StudentHomeSection> = StudentScreen.homeOrder(null, StudentUiLevel.TREE),
    /** 지난번 확인한 단계보다 올라갔으면 새 단계와 새로 생긴 카드. 카드를 닫으면 사라집니다. */
    val levelUp: StudentUiLevel? = null,
    val newSections: List<StudentHomeSection> = emptyList(),
    val studentId: String? = null,
    /** 최근 7일 학습 시간(별 스티커·요일 점)과 연속 학습 일수. */
    val week: List<DayMinutes> = emptyList(),
    val streak: Int = 0,
)
