package com.nextstep.app.ui.parent

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
    val stage: com.nextstep.app.domain.growth.GrowthStage? = null,
    val gradeLabel: String? = null,
    /** 오늘의 부모 팁과 경험 제안. 단계가 없으면 null. */
    val stageTip: String? = null,
    val stageExperience: String? = null,
    /** 여정에서 지금 준비하거나 놓친 항목 (최대 3개). */
    val journeyNow: List<com.nextstep.app.domain.journey.JourneyItem> = emptyList(),
    val hasBirthDate: Boolean = false,
    val today: java.time.LocalDate = java.time.LocalDate.now(),
)
