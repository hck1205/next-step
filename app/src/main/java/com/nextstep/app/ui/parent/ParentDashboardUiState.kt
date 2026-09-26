package com.nextstep.app.ui.parent

import com.nextstep.app.data.prefs.LinkedChild
import com.nextstep.app.domain.mission.MissionFocus
import com.nextstep.app.domain.project.ProjectProgress
import com.nextstep.app.domain.goaltree.GoalNode
import com.nextstep.app.domain.selfdirection.WeekAccess
import com.nextstep.app.domain.selfdirection.WeekStatus
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.domain.stats.BalanceReport
import com.nextstep.app.domain.stats.EventOccurrence
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.domain.stats.UpcomingExam

/** 학부모 첫 화면 상태. 단순한 홈이 보여 주는 값만 둡니다. */
data class ParentDashboardUiState(
    val studentName: String = "",
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val subjects: List<SubjectEntity> = emptyList(),
    val weekMinutes: Int = 0,
    val pendingTasks: List<TaskEntity> = emptyList(),
    val overdueCount: Int = 0,
    val upcomingExams: List<UpcomingExam> = emptyList(),
    val streak: Int = 0,
    val stage: GrowthStage? = null,
    /** 여정에서 지금 준비하거나 놓친 항목 (최대 3개). */
    val journeyNow: List<JourneyItem> = emptyList(),
    val hasBirthDate: Boolean = false,
    val today: LocalDate = DateUtils.today(),
    /** 오늘 일정(반복 포함). */
    val todayEvents: List<EventOccurrence> = emptyList(),
    /** 균형 요약. 상태 문장의 근거. */
    val balance: BalanceReport? = null,
    /** 현재 구간 표기 (예: 초3 2학기). */
    val periodLabel: String? = null,
    /** 날짜 목표의 다음 한 걸음(3개까지). */
    val missionFocus: List<MissionFocus> = emptyList(),
    /** 진행 중인 교육 프로젝트의 오늘 루틴(3개까지). */
    val routines: List<ProjectProgress> = emptyList(),
    /** 사람이 만든 목표 중 먼저 챙길 것(3개까지). */
    val goalFocus: List<GoalNode> = emptyList(),
    /** 아이의 이번 주 계획(자기주도 한 바퀴)과 학부모가 할 수 있는 것. */
    val week: WeekStatus? = null,
    val weekAccess: WeekAccess = WeekAccess(),
    /** 이 기기에 연결된 자녀들과 지금 보고 있는 자녀. */
    val children: List<LinkedChild> = emptyList(),
    val activeFamilyId: String? = null,
) {
    /** 첫 화면의 상태 문장: 균형 판단 + 챙길 것 수. 숫자 대신 문장으로. */
    val statusHeadline: String get() {
        val base = balance?.headline ?: "이번 주 기록이 쌓이면 상태를 알려 드려요"
        val pending = journeyNow.size + overdueCount
        return when (pending) {
            0 -> base
            1 -> "$base\n챙길 것 하나만 남았어요"
            else -> "$base\n챙길 것 ${pending}개가 있어요"
        }
    }
    /** 상태 카드의 맥락 줄: 이번 주 · 구간. */
    val statusContext: String get() = listOfNotNull("이번 주", periodLabel ?: stage?.label).joinToString(" · ")
}
