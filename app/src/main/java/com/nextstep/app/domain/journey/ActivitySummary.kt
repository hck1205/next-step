package com.nextstep.app.domain.journey

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.model.ActivityType
import java.time.LocalDate

/** 활동 기록을 종류·구간별로 요약합니다. 순수 함수입니다. */
object ActivitySummary {

    fun live(activities: List<ActivityEntity>): List<ActivityEntity> = activities.filter { !it.deleted }.sortedByDescending { it.date }

    /** 종류별 개수. 0개인 종류는 빠집니다. */
    fun countByType(activities: List<ActivityEntity>): Map<ActivityType, Int> = live(activities).groupingBy { it.type }.eachCount()

    /** 이어지는 활동(취미·동아리) 중 끝나지 않은 것. */
    fun ongoing(activities: List<ActivityEntity>): List<ActivityEntity> = live(activities).filter { it.isOngoing }

    /** 활동을 시작일이 속한 구간에 배정합니다. 달력 밖의 활동은 가장 가까운 끝 구간으로. */
    fun byPeriod(activities: List<ActivityEntity>, periods: List<JourneyPeriod>): Map<String, List<ActivityEntity>> {
        if (periods.isEmpty()) return emptyMap()
        return live(activities).groupBy { a ->
            val date = LocalDate.ofEpochDay(a.date)
            when {
                date.isBefore(periods.first().start) -> periods.first().key
                date.isAfter(periods.last().end) -> periods.last().key
                else -> periods.first { date in it }.key
            }
        }
    }

    /** 이번 구간에 기록된 활동 수. 대시보드의 "이번 학기 경험" 지표. */
    fun countInPeriod(activities: List<ActivityEntity>, period: JourneyPeriod?): Int =
        if (period == null) 0 else live(activities).count { LocalDate.ofEpochDay(it.date) in period }

    /** 활동 기간(일). 하루짜리는 1, 진행 중이면 오늘까지. */
    fun durationDays(activity: ActivityEntity, today: LocalDate): Long {
        val start = LocalDate.ofEpochDay(activity.date)
        val end = activity.endDate?.let { LocalDate.ofEpochDay(it) } ?: if (activity.isOngoing) today else start
        return (end.toEpochDay() - start.toEpochDay() + 1).coerceAtLeast(1)
    }
}
