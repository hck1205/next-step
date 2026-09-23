package com.nextstep.app.ui.parent

import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.domain.stats.BalanceReport
import com.nextstep.app.domain.stats.EventOccurrence
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.domain.insight.Insight
import com.nextstep.app.domain.insight.Talent
import com.nextstep.app.domain.stats.DayMinutes
import com.nextstep.app.domain.stats.SubjectMinutes
import com.nextstep.app.domain.stats.SubjectProgress
import com.nextstep.app.domain.stats.SubjectScore
import com.nextstep.app.domain.stats.UpcomingExam

data class ParentDashboardUiState(
    val parentName: String = "",
    val studentName: String = "",
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val subjects: List<SubjectEntity> = emptyList(),
    val todayMinutes: Int = 0,
    val weekMinutes: Int = 0,
    val weekGoalMinutes: Int = 0,
    val daily: List<DayMinutes> = emptyList(),
    val weeklyBySubject: List<SubjectMinutes> = emptyList(),
    val pendingTasks: List<TaskEntity> = emptyList(),
    val overdueCount: Int = 0,
    val upcomingExams: List<UpcomingExam> = emptyList(),
    val recentGrades: List<GradeEntity> = emptyList(),
    val scores: List<SubjectScore> = emptyList(),
    val progress: List<SubjectProgress> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val insights: List<Insight> = emptyList(),
    val talents: List<Talent> = emptyList(),
    val streak: Int = 0,
    val roadmapDone: Int = 0,
    val roadmapTotal: Int = 0,
    val mentorCount: Int = 0,
    val parentCount: Int = 0,
    val stage: GrowthStage? = null,
    val gradeLabel: String? = null,
    /** 오늘의 부모 팁과 경험 제안. 단계가 없으면 null. */
    val stageTip: String? = null,
    val stageExperience: String? = null,
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
