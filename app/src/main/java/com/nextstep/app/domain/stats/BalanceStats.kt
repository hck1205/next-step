package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.growth.GrowthGuide
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.ActivitySummary
import com.nextstep.app.domain.journey.JourneyPeriod
import java.time.LocalDate

/**
 * 균형 지표. 학습 시간을 성장 단계의 권장선과 비교하고(또래 비교 없음), 자기주도 비율과 경험 수를 셉니다.
 * 권장선을 넘으면 "줄이기" 가 나갑니다: 이 앱은 더 하라고 밀지 않습니다.
 */
object BalanceStats {
    /** 권장선의 이 비율 아래면 "조금 더", 이 비율(상한) 위면 "줄이기". */
    private const val LOW_RATIO = 0.5f
    private const val HIGH_RATIO = 1.2f
    private const val SELF_DIRECTED_WINDOW_DAYS = 30L

    fun report(
        stage: GrowthStage?,
        sessions: List<StudySessionEntity>,
        tasks: List<TaskEntity>,
        activities: List<ActivityEntity>,
        currentPeriod: JourneyPeriod?,
        today: LocalDate,
    ): BalanceReport {
        val week = StudyStats.weekMinutes(sessions)
        val recommended = recommendedWeekMinutes(stage)
        return BalanceReport(
            weekMinutes = week,
            recommendedWeekMinutes = recommended,
            studyVerdict = verdict(week, recommended),
            selfDirectedRatio = selfDirectedRatio(tasks, today),
            experiencesThisPeriod = ActivitySummary.countInPeriod(activities, currentPeriod),
            streak = StudyStats.studyStreak(sessions),
        )
    }

    /** 단계의 계획 기본값(세션 길이 × 하루 세션 수 × 주당 일수)이 주간 권장선입니다. */
    fun recommendedWeekMinutes(stage: GrowthStage?): Int {
        if (stage == null) return 0
        val plan = GrowthGuide.defaultPlanOptions(stage)
        val daysPerWeek = if (plan.days == 0) 0 else if (plan.includeWeekend) 7 else 5
        return plan.sessionMinutes * plan.sessionsPerDay * daysPerWeek
    }

    fun verdict(weekMinutes: Int, recommended: Int): BalanceVerdict = when {
        recommended == 0 -> BalanceVerdict.NONE
        weekMinutes > recommended * HIGH_RATIO -> BalanceVerdict.LESS
        weekMinutes < recommended * LOW_RATIO -> BalanceVerdict.MORE
        else -> BalanceVerdict.WITHIN
    }

    /** 최근 30일(마감일 기준) 할 일 중 학생이 만든 비율. */
    fun selfDirectedRatio(tasks: List<TaskEntity>, today: LocalDate): Float? {
        val from = today.minusDays(SELF_DIRECTED_WINDOW_DAYS).toEpochDay()
        val recent = tasks.filter { !it.deleted && it.dueDate >= from && it.dueDate <= today.plusDays(SELF_DIRECTED_WINDOW_DAYS).toEpochDay() }
        if (recent.isEmpty()) return null
        return recent.count { it.isStudentMade }.toFloat() / recent.size
    }
}
