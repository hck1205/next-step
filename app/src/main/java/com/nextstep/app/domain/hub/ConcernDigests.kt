package com.nextstep.app.domain.hub

import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.live
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.domain.familycalendar.FamilyOccurrence
import com.nextstep.app.domain.goaltree.GoalNode
import com.nextstep.app.domain.goaltree.WeekRate
import com.nextstep.app.domain.health.GrowthSignalLevel
import com.nextstep.app.domain.health.GrowthSummary
import com.nextstep.app.domain.insight.AptitudeSignal
import com.nextstep.app.domain.mission.MissionFocus
import com.nextstep.app.domain.project.ProjectPace
import com.nextstep.app.domain.project.ProjectProgress
import com.nextstep.app.domain.stats.ReviewItem
import com.nextstep.app.domain.stats.ReviewReason
import com.nextstep.app.domain.stats.ScoreStats
import com.nextstep.app.domain.stats.SubjectProgress
import com.nextstep.app.domain.text.compact
import com.nextstep.app.domain.text.ratioOrNull
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import kotlin.math.roundToInt

/** 관심사마다 타일 한 장을 만듭니다. 숫자는 타일당 하나, 비교 대상은 지난 기록뿐입니다. */
object ConcernDigests {
    /** 성적 평균에 쓰는 최근 시험 수. */
    const val RECENT_GRADES = 5

    /** 공부: 이번 주 시간 · 복습한 단원. 차트는 최근 7일 막대([daily], 오래된 날부터). */
    fun study(weekMinutes: Int, progress: List<SubjectProgress>, daily: List<Int> = emptyList()): ConcernDigest {
        val total = progress.sumOf { it.total }
        val reviewed = progress.sumOf { it.reviewed }
        return ConcernDigest(
            concern = Concern.STUDY,
            headline = if (weekMinutes == 0) "이번 주 아직 0분" else "이번 주 ${DateUtils.formatMinutes(weekMinutes)}",
            detail = if (total == 0) null else "복습 $reviewed/${total}단원",
            attention = weekMinutes == 0,
            chart = daily.takeIf { d -> d.any { it > 0 } }?.let { DigestChart.Bars(it) },
        )
    }

    fun exams(focus: List<MissionFocus>, grades: List<GradeEntity>): ConcernDigest {
        val next = focus.minByOrNull { it.daysLeft }
        val recent = grades.live().sortedByDescending { it.date }.take(RECENT_GRADES)
        return ConcernDigest(
            concern = Concern.EXAMS,
            headline = next?.let { "${it.goal.title} ${DateUtils.dDay(it.daysLeft, pastLabel = "지남")}" } ?: "다가오는 시험 없음",
            detail = ScoreStats.averagePercent(recent)?.let { "최근 ${recent.size}번 평균 ${it.roundToInt()}점" },
            attention = focus.any { it.overdueSteps > 0 },
        )
    }

    /** 성장: 키 · 자라는 속도. 차트는 키 기록 흐름([heights], 오래된 것부터 cm). */
    fun growth(summary: GrowthSummary?, heights: List<Double> = emptyList()): ConcernDigest = ConcernDigest(
        concern = Concern.GROWTH,
        headline = summary?.heightCm?.let { "키 ${it.compact()}cm" } ?: "아직 기록 없음",
        detail = summary?.heightVelocityCmPerYear?.let { "1년에 ${it.compact()}cm 속도" },
        attention = summary?.signals.orEmpty().any { it.level == GrowthSignalLevel.CHECK },
        chart = heights.takeIf { it.size >= 2 }?.let { h -> DigestChart.Line(h.map { (it * TENTHS).roundToInt() }) },
    )

    /** 우리 가족: 오늘 가족 일정 수(없으면 다가오는 일정 수)와 가장 가까운 한 건. 주의 점은 없습니다(가족 일정은 챙길 일이 아니라 알 일). */
    fun family(ahead: List<FamilyOccurrence>, today: LocalDate): ConcernDigest {
        val todays = ahead.count { it.date == today }
        val first = ahead.firstOrNull()
        return ConcernDigest(
            concern = Concern.FAMILY,
            headline = when {
                todays > 0 -> "오늘 가족 일정 ${todays}개"
                ahead.isNotEmpty() -> "다가오는 가족 일정 ${ahead.size}개"
                else -> "다가오는 가족 일정 없음"
            },
            detail = first?.let { "${if (it.date == today) "오늘" else DateUtils.formatShortDate(it.start)} · ${it.event.title}" },
        )
    }

    fun discover(activitiesThisPeriod: Int, signals: List<AptitudeSignal>): ConcernDigest = ConcernDigest(
        concern = Concern.DISCOVER,
        headline = if (activitiesThisPeriod == 0) "이번 학기 활동 없음" else "이번 학기 활동 ${activitiesThisPeriod}개",
        detail = signals.firstOrNull()?.let { "${it.domain.label} 쪽에 신호" },
        attention = activitiesThisPeriod == 0,
    )

    /** 배울 것: 복습할 단원 수와 첫 단원. 차트는 전체 단원 중 복습까지 한 몫([progress]). */
    fun learn(review: List<ReviewItem>, progress: List<SubjectProgress> = emptyList()): ConcernDigest {
        val total = progress.sumOf { it.total }
        val first = review.firstOrNull()
        return ConcernDigest(
            concern = Concern.LEARN,
            headline = if (review.isEmpty()) "복습할 단원 없음" else "복습할 단원 ${review.size}개",
            detail = first?.let { "${it.subject.name} · ${it.topic.title}" },
            attention = review.any { it.reason == ReviewReason.LOW_CONFIDENCE },
            chart = ratioOrNull(progress.sumOf { it.reviewed }, total)?.let { DigestChart.Meter(it) },
        )
    }

    /** 교육 프로젝트: 진행 중인 수와, 늦어진 것(없으면 첫 프로젝트)의 지금 단계. 이번 주 기록이 없거나 늦어지면 주의. 차트는 계획대로 가는 몫. */
    fun project(progress: List<ProjectProgress>): ConcernDigest {
        val open = progress.filter { !it.isDone }
        val focus = open.firstOrNull { it.pace == ProjectPace.BEHIND } ?: open.firstOrNull()
        return ConcernDigest(
            concern = Concern.PROJECT,
            headline = if (open.isEmpty()) "진행 중인 프로젝트 없음" else "프로젝트 ${open.size}개 진행 중",
            detail = focus?.let { p -> "${p.plan.title} · ${p.current?.title ?: ""} · ${p.pace.label}" },
            attention = open.any { it.pace == ProjectPace.BEHIND || it.weekMinutes == 0 },
            chart = ratioOrNull(open.count { it.pace != ProjectPace.BEHIND }, open.size)?.let { DigestChart.Meter(it) },
        )
    }

    /** 목표·할 일: 이번 주 마감 할 일 중 끝낸 수, 밀린 할 일 → 오래 멈춘 목표 → 진행 중인 목표 수. 차트는 주별 달성 막대([weeks], 오래된 주부터). */
    fun plan(goals: List<GoalNode>, week: WeekRate?, overdue: Int, weeks: List<WeekRate> = emptyList()): ConcernDigest {
        val active = goals.filter { it.goal.status == GoalStatus.ACTIVE }
        val idle = active.filter { it.isIdle }.maxByOrNull { it.idleDays }
        return ConcernDigest(
            concern = Concern.PLAN,
            headline = if (week == null || week.due == 0) "이번 주 마감 할 일 없음" else "이번 주 할 일 ${week.done}/${week.due}",
            detail = when {
                overdue > 0 -> "밀린 할 일 ${overdue}개"
                idle != null -> "${idle.goal.title} · ${idle.idleDays}일째 그대로"
                active.isNotEmpty() -> "목표 ${active.size}개 진행 중"
                else -> null
            },
            attention = overdue > 0 || idle != null,
            chart = weeks.takeIf { w -> w.any { it.due > 0 } }?.let { w -> DigestChart.Bars(w.map { it.percent }) },
        )
    }

    private const val TENTHS = 10
}
