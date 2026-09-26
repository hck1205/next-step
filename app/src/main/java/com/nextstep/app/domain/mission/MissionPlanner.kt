package com.nextstep.app.domain.mission

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.domain.journey.GoalPlanner
import com.nextstep.app.domain.journey.JourneyPeriod
import com.nextstep.app.domain.journey.PeriodCalendar
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 날짜가 정해진 목표를 세부 단계로 쪼개고, 지금 할 한 걸음을 고릅니다. 순수 함수입니다.
 * 단계 날짜는 목표일에서 거꾸로 잡고, 준비 기간이 설계보다 짧으면 앞 단계를 비율대로 당겨 오늘부터 시작합니다.
 */
object MissionPlanner {

    fun create(
        kind: MissionKind,
        target: LocalDate,
        today: LocalDate,
        periods: List<JourneyPeriod>,
        subject: String? = null,
        createdByRole: String = "",
    ): Pair<GoalEntity, List<GoalStepEntity>> {
        val template = MissionCatalog.of(kind)
        val goal = GoalEntity(
            familyId = "", trackId = kind.trackId, title = template.titleFor(subject), area = template.area.name,
            targetDate = target.toEpochDay(), createdByRole = createdByRole,
        )
        val steps = schedule(template, target, today).mapIndexed { index, (step, due) ->
            GoalStepEntity(
                familyId = "", goalId = goal.id, periodKey = PeriodCalendar.periodOf(periods, due)?.key ?: "",
                orderIndex = index, title = step.title, detail = step.detail, dueDate = due.toEpochDay(),
            )
        }
        return goal to steps
    }

    /** 단계마다 마감일. 목표일 뒤 단계(음수)와 여유가 충분한 경우는 설계 그대로입니다. */
    fun schedule(template: MissionTemplate, target: LocalDate, today: LocalDate): List<Pair<MissionStepTemplate, LocalDate>> {
        val span = template.spanDays
        val available = ChronoUnit.DAYS.between(today, target).toInt().coerceAtLeast(0)
        return template.steps.map { step ->
            val offset = when {
                step.daysBefore <= 0 || span <= available -> step.daysBefore
                available == 0 -> 0
                else -> (step.daysBefore.toLong() * available / span).toInt().coerceAtLeast(1)
            }
            step to target.minusDays(offset.toLong())
        }
    }

    fun kindOf(goal: GoalEntity): MissionKind? = MissionKind.ofTrackId(goal.trackId)

    /** 날짜가 있는 목표. 사람이 만든 목표 트리의 목표는 기한이 있어도 미션이 아닙니다. */
    fun isMission(goal: GoalEntity): Boolean = goal.targetDate != null && !GoalTree.isTreeGoal(goal)

    /** 아직 끝내지 않은 첫 단계. */
    fun nextStep(steps: List<GoalStepEntity>): GoalStepEntity? = steps.filter { it.isOpen }.minByOrNull { it.orderIndex }

    fun daysLeft(goal: GoalEntity, today: LocalDate): Int? = goal.targetDate?.let { (it - today.toEpochDay()).toInt() }

    /** 마감이 지났는데 끝내지 않은 단계 수. */
    fun overdueCount(steps: List<GoalStepEntity>, today: LocalDate): Int =
        steps.count { it.isOpen && (it.dueDate ?: Long.MAX_VALUE) < today.toEpochDay() }

    /** 진행 중인 날짜 목표마다 다음 한 걸음. 목표일이 가까운 순서로 [limit]개까지. */
    fun focus(goals: List<GoalEntity>, steps: List<GoalStepEntity>, today: LocalDate, limit: Int = FOCUS_LIMIT): List<MissionFocus> =
        goals.filter { !it.deleted && it.status == GoalStatus.ACTIVE && isMission(it) }
            .sortedBy { it.targetDate }
            .mapNotNull { goal ->
                val mine = GoalPlanner.stepsOf(goal, steps)
                val next = nextStep(mine) ?: return@mapNotNull null
                MissionFocus(
                    goal = goal, kind = kindOf(goal), nextStep = next, daysLeft = daysLeft(goal, today) ?: 0,
                    overdueSteps = overdueCount(mine, today), progress = GoalPlanner.progress(mine),
                    stepCount = mine.size, doneCount = mine.count { it.status == MilestoneStatus.DONE },
                )
            }
            .take(limit)

    private val GoalStepEntity.isOpen: Boolean get() = !deleted && status != MilestoneStatus.DONE && status != MilestoneStatus.SKIPPED

    private const val FOCUS_LIMIT = 3
}
