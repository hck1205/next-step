package com.nextstep.app.ui.journey

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.ActivitySummary
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.domain.journey.JourneyPeriod
import com.nextstep.app.domain.journey.JourneyPhase
import com.nextstep.app.domain.journey.MilestoneCategory
import java.time.LocalDate

/** 구간(학기) 하나의 타임라인 칸: 그 구간에 마감이 있는 이정표와 그 구간에 배정된 목표 단계. */
data class PeriodSection(
    val period: JourneyPeriod,
    val isCurrent: Boolean,
    val isPast: Boolean,
    val milestones: List<JourneyItem>,
    val steps: List<StepView>,
    /** 그 구간에 시작한 활동 기록. */
    val activities: List<ActivityEntity> = emptyList(),
)

/** 목표 단계 + 목표 제목. 화면은 이것만 봅니다. */
data class StepView(val step: GoalStepEntity, val goalTitle: String)

data class JourneyUiState(
    val studentName: String = "",
    /** 생년월일을 저장할 학생 행. */
    val studentMemberId: String? = null,
    val stage: GrowthStage? = null,
    /** "만 3세 4개월" 같은 나이 표기. 생년월일이 없으면 null. */
    val ageLabel: String? = null,
    val hasBirthDate: Boolean = false,
    val today: LocalDate = LocalDate.now(),
    val items: List<JourneyItem> = emptyList(),
    val periods: List<JourneyPeriod> = emptyList(),
    val currentPeriodKey: String? = null,
    val goals: List<GoalEntity> = emptyList(),
    val steps: List<GoalStepEntity> = emptyList(),
    val activities: List<ActivityEntity> = emptyList(),
    /** 현재 구간의 교과 커리큘럼(학령기만). */
    val curriculum: com.nextstep.app.domain.curriculum.TermCurriculum? = null,
    val completion: Float = 0f,
    val filter: MilestoneCategory? = null,
    val showCompleted: Boolean = false,
    /** 지난 구간까지 펼쳐 보기. 기본은 현재 구간부터. */
    val showPast: Boolean = false,
    val loaded: Boolean = false,
) {
    val filtered: List<JourneyItem> get() = items.filter { filter == null || it.category == filter }

    private val visibleItems: List<JourneyItem> get() = filtered.filter { showCompleted || it.isOpen }

    /**
     * 구간별 타임라인. 이정표는 마감일이 속한 구간에, 단계는 periodKey 로 배정됩니다.
     * 구간 밖(달력 이전·이후)의 이정표는 가장 가까운 끝 구간에 붙입니다.
     */
    val periodSections: List<PeriodSection> get() {
        if (periods.isEmpty()) return emptyList()
        val goalTitles = goals.associate { it.id to it.title }
        val currentIndex = periods.indexOfFirst { it.key == currentPeriodKey }
        val stepsByPeriod = steps.filter { !it.deleted && (showCompleted || (it.status != MilestoneStatus.DONE && it.status != MilestoneStatus.SKIPPED)) }
            .filter { it.goalId in goalTitles }.groupBy { it.periodKey }
        val itemsByPeriod = visibleItems.groupBy { item -> periodIndexOf(item.dueDate) }
        val activitiesByPeriod = ActivitySummary.byPeriod(activities, periods)
        return periods.mapIndexed { index, period ->
            PeriodSection(
                period = period,
                isCurrent = index == currentIndex,
                isPast = currentIndex >= 0 && index < currentIndex,
                milestones = itemsByPeriod[index].orEmpty().sortedBy { it.dueDate },
                steps = stepsByPeriod[period.key].orEmpty().sortedBy { it.orderIndex }.map { StepView(it, goalTitles.getValue(it.goalId)) },
                activities = activitiesByPeriod[period.key].orEmpty(),
            )
        }.filter { (showPast || !it.isPast) && (it.milestones.isNotEmpty() || it.steps.isNotEmpty() || it.activities.isNotEmpty() || it.isCurrent) }
    }

    /** 생년월일이 없을 때: 단계 없이 상태별로만 묶습니다. */
    val phaseSections: List<Pair<JourneyPhase, List<JourneyItem>>> get() = JourneyPhase.entries
        .filter { showCompleted || (it != JourneyPhase.DONE && it != JourneyPhase.SKIPPED) }
        .map { phase -> phase to filtered.filter { it.phase(today) == phase } }
        .filter { it.second.isNotEmpty() }

    val overdueCount: Int get() = items.count { it.phase(today) == JourneyPhase.OVERDUE }
    val nowCount: Int get() = items.count { it.phase(today) == JourneyPhase.NOW }
    val pastSectionCount: Int get() = periods.indexOfFirst { it.key == currentPeriodKey }.coerceAtLeast(0)

    private fun periodIndexOf(date: LocalDate): Int = when {
        date.isBefore(periods.first().start) -> 0
        date.isAfter(periods.last().end) -> periods.lastIndex
        else -> periods.indexOfFirst { date in it }
    }
}
