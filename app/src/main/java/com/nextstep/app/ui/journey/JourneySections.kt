package com.nextstep.app.ui.journey

import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.domain.journey.ActivitySummary
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.domain.journey.JourneyPhase
import java.time.LocalDate

/**
 * 여정 상태의 파생 값(구간별 묶음, 상태별 묶음, 개수)을 한 번에 계산합니다.
 * 상태 객체의 getter 로 두면 리컴포지션마다 다시 묶으므로 ViewModel 이 이 함수를 거쳐 상태를 만듭니다.
 */
internal object JourneySections {
    fun apply(s: JourneyUiState): JourneyUiState = s.copy(
        periodSections = periodSections(s),
        phaseSections = phaseSections(s),
        overdueCount = s.items.count { it.phase(s.today) == JourneyPhase.OVERDUE },
        nowCount = s.items.count { it.phase(s.today) == JourneyPhase.NOW },
        pastSectionCount = s.periods.indexOfFirst { it.key == s.currentPeriodKey }.coerceAtLeast(0),
    )

    /**
     * 구간별 타임라인. 이정표는 마감일이 속한 구간에, 단계는 periodKey 로 배정됩니다.
     * 구간 밖(달력 이전·이후)의 이정표는 가장 가까운 끝 구간에 붙입니다.
     */
    private fun periodSections(s: JourneyUiState): List<PeriodSection> {
        val periods = s.periods
        if (periods.isEmpty()) return emptyList()
        val goalTitles = s.goals.associate { it.id to it.title }
        val currentIndex = periods.indexOfFirst { it.key == s.currentPeriodKey }
        val stepsByPeriod = s.steps.filter { !it.deleted && (s.showCompleted || (it.status != MilestoneStatus.DONE && it.status != MilestoneStatus.SKIPPED)) }
            .filter { it.goalId in goalTitles }.groupBy { it.periodKey }
        val visible = s.filtered.filter { s.showCompleted || it.isOpen }
        val itemsByPeriod = visible.groupBy { item -> periodIndexOf(s, item.dueDate) }
        val activitiesByPeriod = ActivitySummary.byPeriod(s.activities, periods)
        return periods.mapIndexed { index, period ->
            PeriodSection(
                period = period,
                isCurrent = index == currentIndex,
                isPast = currentIndex >= 0 && index < currentIndex,
                milestones = itemsByPeriod[index].orEmpty().sortedBy { it.dueDate },
                steps = stepsByPeriod[period.key].orEmpty().sortedBy { it.orderIndex }.map { StepView(it, goalTitles.getValue(it.goalId)) },
                activities = activitiesByPeriod[period.key].orEmpty(),
            )
        }.filter { (s.showPast || !it.isPast) && (it.milestones.isNotEmpty() || it.steps.isNotEmpty() || it.activities.isNotEmpty() || it.isCurrent) }
    }

    /** 생년월일이 없을 때: 단계 없이 상태별로만 묶습니다. */
    private fun phaseSections(s: JourneyUiState): List<Pair<JourneyPhase, List<JourneyItem>>> = JourneyPhase.entries
        .filter { s.showCompleted || (it != JourneyPhase.DONE && it != JourneyPhase.SKIPPED) }
        .map { phase -> phase to s.filtered.filter { it.phase(s.today) == phase } }
        .filter { it.second.isNotEmpty() }

    private fun periodIndexOf(s: JourneyUiState, date: LocalDate): Int = when {
        date.isBefore(s.periods.first().start) -> 0
        date.isAfter(s.periods.last().end) -> s.periods.lastIndex
        else -> s.periods.indexOfFirst { date in it }
    }
}
