package com.nextstep.app.domain.stats

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.goaltree.PlanHistory
import com.nextstep.app.domain.goaltree.RateBy
import com.nextstep.app.domain.goaltree.WeekRate

/**
 * 학부모·멘토 오늘 화면의 차트 값 한 벌([TrendStats.family]). 저장하지 않고 기록에서 계산합니다.
 * [rolling] 은 7일씩 8번(마지막이 최근 7일), [daily] 는 최근 7일(오래된 날부터), [dailyGoal] 은 과목 주 목표 합 ÷ 7.
 */
data class FamilyTrends(
    val rolling: List<Int> = emptyList(),
    val heat: List<HeatWeek> = emptyList(),
    val daily: List<DayMinutes> = emptyList(),
    val bySubject: List<SubjectMinutes> = emptyList(),
    /** 최근 4주 준 사람별 할 일 달성(준 적 있는 사람만, [PlanHistory.byAssigner]). */
    val assigners: List<RateBy> = emptyList(),
    /** 최근 5주 주별 할 일 달성([PlanHistory.weeks]). */
    val weekRates: List<WeekRate> = emptyList(),
    val scores: List<ScoreSeries> = emptyList(),
    val submissions: Submissions = Submissions(0, 0, 0),
    val dailyGoal: Int = 0,
) {
    val recent: Int get() = rolling.lastOrNull() ?: 0
    /** 최근 7일 − 그 전 7일. */
    val recentChange: Int? get() = if (rolling.size < 2) null else rolling[rolling.size - 1] - rolling[rolling.size - 2]
    val hasStudy: Boolean get() = rolling.any { it > 0 }
    val hasTasks: Boolean get() = assigners.any { it.total > 0 }

    /** 최근 4주 할 일 중 학생이 스스로 정한 몫(0~1). */
    val selfShare: Float get() {
        val total = assigners.sumOf { it.total }
        return if (total == 0) 0f else (assigners.firstOrNull { it.key == Role.STUDENT.name }?.total ?: 0).toFloat() / total
    }

    /** 과목마다 마지막 점수의 평균(성적이 없으면 null). */
    val scoreAverage: Int? get() = scores.mapNotNull { it.last }.takeIf { it.isNotEmpty() }?.average()?.toInt()
    /** 과목마다 직전 대비 변화의 평균(비교할 시험이 없으면 null). */
    val scoreChange: Int? get() = scores.mapNotNull { it.change }.takeIf { it.isNotEmpty() }?.average()?.toInt()
}
