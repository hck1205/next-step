package com.nextstep.app.domain.hub

import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.domain.health.GrowthSignalLevel
import com.nextstep.app.domain.health.GrowthSummary
import com.nextstep.app.domain.insight.AptitudeSignal
import com.nextstep.app.domain.mentor.AssignmentReport
import com.nextstep.app.domain.mission.MissionFocus
import com.nextstep.app.domain.project.ProjectPace
import com.nextstep.app.domain.project.ProjectProgress
import com.nextstep.app.domain.stats.ReviewItem
import com.nextstep.app.domain.stats.ReviewReason
import com.nextstep.app.domain.stats.ScoreStats
import com.nextstep.app.domain.stats.SubjectProgress
import com.nextstep.app.domain.text.compact
import com.nextstep.app.domain.time.DateUtils
import kotlin.math.roundToInt

/** 관심사마다 타일 한 장을 만듭니다. 숫자는 타일당 하나, 비교 대상은 지난 기록뿐입니다. */
object ConcernDigests {
    /** 성적 평균에 쓰는 최근 시험 수. */
    const val RECENT_GRADES = 5

    fun study(weekMinutes: Int, progress: List<SubjectProgress>): ConcernDigest {
        val total = progress.sumOf { it.total }
        val reviewed = progress.sumOf { it.reviewed }
        return ConcernDigest(
            concern = Concern.STUDY,
            headline = if (weekMinutes == 0) "이번 주 아직 0분" else "이번 주 ${DateUtils.formatMinutes(weekMinutes)}",
            detail = if (total == 0) null else "복습 $reviewed/${total}단원",
            attention = weekMinutes == 0,
        )
    }

    fun exams(focus: List<MissionFocus>, grades: List<GradeEntity>): ConcernDigest {
        val next = focus.minByOrNull { it.daysLeft }
        val recent = grades.filter { !it.deleted }.sortedByDescending { it.date }.take(RECENT_GRADES)
        return ConcernDigest(
            concern = Concern.EXAMS,
            headline = next?.let { "${it.goal.title} ${dDay(it.daysLeft)}" } ?: "다가오는 시험 없음",
            detail = ScoreStats.averagePercent(recent)?.let { "최근 ${recent.size}번 평균 ${it.roundToInt()}점" },
            attention = focus.any { it.overdueSteps > 0 },
        )
    }

    fun growth(summary: GrowthSummary?): ConcernDigest = ConcernDigest(
        concern = Concern.GROWTH,
        headline = summary?.heightCm?.let { "키 ${it.compact()}cm" } ?: "아직 기록 없음",
        detail = summary?.heightVelocityCmPerYear?.let { "1년에 ${it.compact()}cm 속도" },
        attention = summary?.signals.orEmpty().any { it.level == GrowthSignalLevel.CHECK },
    )

    fun discover(activitiesThisPeriod: Int, signals: List<AptitudeSignal>): ConcernDigest = ConcernDigest(
        concern = Concern.DISCOVER,
        headline = if (activitiesThisPeriod == 0) "이번 학기 활동 없음" else "이번 학기 활동 ${activitiesThisPeriod}개",
        detail = signals.firstOrNull()?.let { "${it.domain.label} 쪽에 신호" },
        attention = activitiesThisPeriod == 0,
    )

    fun learn(review: List<ReviewItem>): ConcernDigest {
        val first = review.firstOrNull()
        return ConcernDigest(
            concern = Concern.LEARN,
            headline = if (review.isEmpty()) "복습할 단원 없음" else "복습할 단원 ${review.size}개",
            detail = first?.let { "${it.subject.name} · ${it.topic.title}" },
            attention = review.any { it.reason == ReviewReason.LOW_CONFIDENCE },
        )
    }

    /** 교육 프로젝트: 진행 중인 수와, 늦어진 것(없으면 첫 프로젝트)의 지금 단계. 이번 주 기록이 없거나 늦어지면 주의. */
    fun project(progress: List<ProjectProgress>): ConcernDigest {
        val open = progress.filter { !it.isDone }
        val focus = open.firstOrNull { it.pace == ProjectPace.BEHIND } ?: open.firstOrNull()
        return ConcernDigest(
            concern = Concern.PROJECT,
            headline = if (open.isEmpty()) "진행 중인 프로젝트 없음" else "프로젝트 ${open.size}개 진행 중",
            detail = focus?.let { p -> "${p.plan.title} · ${p.current?.title ?: ""} · ${p.pace.label}" },
            attention = open.any { it.pace == ProjectPace.BEHIND || it.weekMinutes == 0 },
        )
    }

    fun classwork(report: AssignmentReport): ConcernDigest = ConcernDigest(
        concern = Concern.CLASS,
        headline = if (report.total == 0) "낸 과제 없음" else "과제 ${report.done}/${report.total}",
        detail = when {
            report.overdue.isNotEmpty() -> "밀린 과제 ${report.overdue.size}개"
            report.dueSoon.isNotEmpty() -> "이번 주 마감 ${report.dueSoon.size}개"
            else -> null
        },
        attention = report.overdue.isNotEmpty(),
    )

    private fun dDay(daysLeft: Int): String = when {
        daysLeft > 0 -> "D-$daysLeft"
        daysLeft == 0 -> "D-day"
        else -> "지남"
    }
}
