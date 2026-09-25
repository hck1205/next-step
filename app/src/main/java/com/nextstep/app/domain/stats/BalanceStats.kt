package com.nextstep.app.domain.stats

import com.nextstep.app.domain.growth.YearProfile
import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.domain.growth.GrowthGuide
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.ActivitySummary
import com.nextstep.app.domain.journey.JourneyPeriod
import java.time.LocalDate

/**
 * 균형 지표. 학습 시간을 성장 단계의 권장선과 비교하고(또래 비교 없음), 자기주도 비율과 경험 수를 셉니다.
 * 권장선을 넘으면 "줄이기" 가 나갑니다: 이 앱은 더 하라고 밀지 않습니다. 올해 프로필([YearProfile])이 있으면 해마다 다른 권장선을 씁니다.
 *
 * 영유아기(만 0~6세)는 과열 가드가 하나 더 있습니다. 앉아서 하는 학습 권장선이 0인 시기(영아·유아)에 학습 기록이 있으면 "줄이기",
 * 학원·수업 일정이 단계 상한([classCapWeekMinutes])을 넘어도 "줄이기"입니다. 또래가 얼마나 하는지는 보지 않습니다.
 */
object BalanceStats {
    /** 권장선의 이 비율 아래면 "조금 더", 이 비율(상한) 위면 "줄이기". */
    private const val LOW_RATIO = 0.5f
    private const val HIGH_RATIO = 1.2f
    private const val SELF_DIRECTED_WINDOW_DAYS = 30L
    private const val NEWBORN_CLASS_CAP = 60
    private const val TODDLER_CLASS_CAP = 120
    private const val PRESCHOOL_CLASS_CAP = 300
    private const val DAYS_IN_WEEK = 7L
    private const val MILLIS_PER_MINUTE = 60_000L

    fun report(
        stage: GrowthStage?,
        sessions: List<StudySessionEntity>,
        tasks: List<TaskEntity>,
        activities: List<ActivityEntity>,
        currentPeriod: JourneyPeriod?,
        today: LocalDate,
        events: List<EventEntity> = emptyList(),
        year: YearProfile? = null,
    ): BalanceReport {
        val week = StudyStats.weekMinutes(sessions)
        val recommended = year?.weekMinutes ?: recommendedWeekMinutes(stage)
        val classes = classWeekMinutes(events, today)
        val cap = classCapWeekMinutes(stage)
        return BalanceReport(
            weekMinutes = week,
            recommendedWeekMinutes = recommended,
            studyVerdict = if (stage != null && recommended == 0) (if (week > 0) BalanceVerdict.LESS else BalanceVerdict.NONE) else verdict(week, recommended),
            classWeekMinutes = classes,
            classCapWeekMinutes = cap,
            classVerdict = when {
                cap == null -> BalanceVerdict.NONE
                classes > cap -> BalanceVerdict.LESS
                else -> BalanceVerdict.WITHIN
            },
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

    /**
     * 영유아기 학원·수업(문화센터·놀이 수업·영어 수업 포함) 주간 상한.
     * 영아 주 1시간(문화센터 1회), 유아 주 2시간, 유치원기 주 5시간. 학령기는 null.
     */
    fun classCapWeekMinutes(stage: GrowthStage?): Int? = when (stage) {
        GrowthStage.NEWBORN -> NEWBORN_CLASS_CAP
        GrowthStage.TODDLER -> TODDLER_CLASS_CAP
        GrowthStage.PRESCHOOL -> PRESCHOOL_CLASS_CAP
        else -> null
    }

    /** 이번 주(월~일) 학원·수업 일정의 합(분). 매주 반복 일정도 셉니다. */
    fun classWeekMinutes(events: List<EventEntity>, today: LocalDate): Int {
        val monday = today.minusDays(today.dayOfWeek.value - 1L)
        return (0L until DAYS_IN_WEEK).sumOf { d ->
            StudyStats.eventsOn(monday.plusDays(d), events)
                .filter { !it.event.deleted && (it.event.type == EventType.ACADEMY || it.event.type == EventType.CLASS) }
                .sumOf { ((it.endAt - it.startAt) / MILLIS_PER_MINUTE).toInt() }
        }
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
