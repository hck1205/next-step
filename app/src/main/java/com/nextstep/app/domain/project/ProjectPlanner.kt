package com.nextstep.app.domain.project

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.local.entity.ProjectLogEntity
import com.nextstep.app.data.local.entity.newId
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 교육 프로젝트를 목표 저장소에 올리고, 기록으로 지금 단계·이번 주 양·계획 대비 속도를 계산합니다. 순수 함수입니다.
 *
 * 저장 방식: 프로젝트 하나 = GoalEntity(trackId = "project:<plan.id>", targetDate 없음 → 날짜 목표(미션)와 섞이지 않음),
 * 단계 하나 = GoalStepEntity(periodKey = "project:<phase.key>" → 여정의 학기 단계와 섞이지 않음, dueDate = 계획한 끝날).
 * 루틴 기록은 ProjectLogEntity 에 따로 남깁니다.
 */
object ProjectPlanner {
    const val PREFIX = "project:"

    fun isProject(goal: GoalEntity): Boolean = goal.trackId?.startsWith(PREFIX) == true

    fun planOf(goal: GoalEntity): ProjectPlan? =
        goal.trackId?.takeIf { it.startsWith(PREFIX) }?.removePrefix(PREFIX)?.let { ProjectCatalog.byId[it] }

    fun phaseKeyOf(step: GoalStepEntity): String? = step.periodKey.takeIf { it.startsWith(PREFIX) }?.removePrefix(PREFIX)

    /**
     * 추천에 쓰는 만 나이(개월). 생년월일이 있으면 그대로, 없으면 학년으로 어림합니다(초1 3월 = [FIRST_GRADE_MONTHS]개월).
     */
    fun ageMonths(birthDate: LocalDate?, gradeYear: Int?, today: LocalDate): Int? {
        if (birthDate != null) return ChronoUnit.MONTHS.between(birthDate, today).toInt().takeIf { it >= 0 }
        val grade = gradeYear?.takeIf { it > 0 } ?: return null
        val sinceMarch = (today.monthValue - SCHOOL_YEAR_START_MONTH + MONTHS_PER_YEAR) % MONTHS_PER_YEAR
        return FIRST_GRADE_MONTHS + (grade - 1) * MONTHS_PER_YEAR + sinceMarch
    }

    /** 아이 나이에 맞는 시작 단계: 이미 시작 나이가 지난 단계 중 마지막. 나이를 모르거나 첫 단계보다 어리면 0. */
    fun suggestedStart(plan: ProjectPlan, ageMonths: Int?): Int {
        if (ageMonths == null) return 0
        return plan.phases.indexOfLast { it.fromMonths <= ageMonths }.coerceAtLeast(0)
    }

    /**
     * 아직 시작하지 않은 프로젝트 가운데 지금 나이에 맞는 것. 지금 할 수 있는 것(시작 나이가 지남)이 먼저, 곧 시작할 것이 다음.
     * 마지막 단계를 시작할 나이에서 1년이 더 지난 프로젝트는 뺍니다.
     */
    fun recommend(plans: List<ProjectPlan>, ageMonths: Int?, started: Set<String>): List<ProjectPlan> {
        val open = plans.filter { it.id !in started }
        if (ageMonths == null) return open
        return open.filter { ageMonths <= it.phases.last().fromMonths + MONTHS_PER_YEAR }
            .sortedWith(compareBy<ProjectPlan> { it.startMonths > ageMonths }.thenBy { it.startMonths })
    }

    /** [startIndex] 단계부터 [startDate] 에 시작해 단계마다 보통 기간을 이어 붙인 일정. 앞 단계는 건너뛴 것으로 날짜가 없습니다. */
    fun schedule(plan: ProjectPlan, startIndex: Int, startDate: LocalDate): List<PhaseSlot> {
        var cursor = startDate
        return plan.phases.mapIndexed { i, phase ->
            if (i < startIndex) {
                PhaseSlot(i, phase, null, null)
            } else {
                val end = cursor.plusWeeks(phase.weeks.toLong()).minusDays(1)
                PhaseSlot(i, phase, cursor, end).also { cursor = end.plusDays(1) }
            }
        }
    }

    /** 프로젝트를 시작합니다: 목표 하나와 단계들. 시작 단계 앞은 건너뜀, 시작 단계는 진행 중. familyId 는 저장소가 채웁니다. */
    fun start(plan: ProjectPlan, startIndex: Int, today: LocalDate, createdByRole: String): Pair<GoalEntity, List<GoalStepEntity>> {
        val from = startIndex.coerceIn(0, plan.phases.lastIndex)
        val goal = GoalEntity(
            id = newId(), familyId = "", trackId = PREFIX + plan.id, title = plan.title, area = plan.category.goalArea.name,
            description = plan.goal, createdByRole = createdByRole,
        )
        val steps = schedule(plan, from, today).map { slot ->
            GoalStepEntity(
                familyId = "", goalId = goal.id, periodKey = PREFIX + slot.phase.key, orderIndex = slot.index, title = slot.phase.title,
                detail = slot.phase.checkpoint, dueDate = slot.end?.toEpochDay(),
                status = when {
                    slot.index < from -> MilestoneStatus.SKIPPED
                    slot.index == from -> MilestoneStatus.IN_PROGRESS
                    else -> MilestoneStatus.UPCOMING
                },
            )
        }
        return goal to steps
    }

    /** 진행 중인(보관하지 않은) 프로젝트마다 지금 모습. 카탈로그에 없는 프로젝트는 뺍니다. */
    fun progressAll(goals: List<GoalEntity>, steps: List<GoalStepEntity>, logs: List<ProjectLogEntity>, today: LocalDate): List<ProjectProgress> =
        goals.filter { !it.deleted && it.status != GoalStatus.ARCHIVED && isProject(it) }
            .sortedBy { it.createdAt }
            .mapNotNull { goal -> planOf(goal)?.let { plan -> progress(plan, goal, steps, logs, today) } }

    /**
     * 프로젝트 하나의 지금 모습.
     * - 지금 단계 = 통과(DONE)·건너뜀(SKIPPED)이 아닌 첫 단계.
     * - 속도 = 계획표에서 오늘 있어야 할 단계(끝날이 오늘 이후인 첫 단계)와 비교. 앞서면 빠름, 뒤지면 늦음.
     * - 예상 끝 = 계획한 끝날을 늦은 날수만큼 뒤로, 앞선 날수만큼 앞으로 옮긴 날.
     */
    fun progress(plan: ProjectPlan, goal: GoalEntity, steps: List<GoalStepEntity>, logs: List<ProjectLogEntity>, today: LocalDate): ProjectProgress {
        val byKey = steps.filter { it.goalId == goal.id && !it.deleted }.associateBy { phaseKeyOf(it) }
        val stepOf = { i: Int -> byKey[plan.phases[i].key] }
        val closed = { i: Int -> stepOf(i)?.status.let { it == MilestoneStatus.DONE || it == MilestoneStatus.SKIPPED } }
        val currentIndex = plan.phases.indices.firstOrNull { !closed(it) } ?: plan.phases.size
        val current = plan.phases.getOrNull(currentIndex)

        val mine = logs.filter { it.goalId == goal.id && !it.deleted }
        val monday = today.with(DayOfWeek.MONDAY).toEpochDay()
        val week = mine.filter { it.date in monday..today.toEpochDay() }
        val todays = mine.filter { it.date == today.toEpochDay() && it.phaseKey == current?.key }

        val dues = plan.phases.indices.map { stepOf(it)?.takeIf { s -> s.status != MilestoneStatus.SKIPPED }?.dueDate }
        val targetDate = dues.lastOrNull { it != null }?.let { LocalDate.ofEpochDay(it) }
        val shiftDays = shiftDays(currentIndex, dues, today)
        val pace = when {
            current == null -> ProjectPace.DONE
            shiftDays > 0 -> ProjectPace.BEHIND
            shiftDays < 0 -> ProjectPace.AHEAD
            else -> ProjectPace.ON_TRACK
        }
        return ProjectProgress(
            goalId = goal.id, plan = plan, currentIndex = currentIndex,
            passed = plan.phases.indices.count { stepOf(it)?.status == MilestoneStatus.DONE },
            weekMinutes = week.sumOf { it.minutes }, weekTarget = current?.weeklyMinutes ?: 0,
            todayMinutes = todays.sumOf { it.minutes }, todayDoneItems = todays.map { it.item }.toSet(),
            activeDaysThisWeek = week.map { it.date }.distinct().size,
            pace = pace, targetDate = targetDate,
            projectedEnd = if (current == null) null else targetDate?.plusDays(shiftDays),
        )
    }

    /** 저장된 단계의 계획 일정(타임라인용). 시작날은 앞 단계 끝날 다음 날, 첫 단계는 끝날에서 보통 기간만큼 거슬러 간 날. 건너뛴 단계는 날짜 없음. */
    fun slotsOf(plan: ProjectPlan, goal: GoalEntity, steps: List<GoalStepEntity>): List<PhaseSlot> {
        val keyed = steps.filter { it.goalId == goal.id && !it.deleted }.associateBy { phaseKeyOf(it) }
        var cursor: LocalDate? = null
        return plan.phases.mapIndexed { i, phase ->
            val step = keyed[phase.key]
            val end = step?.takeIf { it.status != MilestoneStatus.SKIPPED }?.dueDate?.let { LocalDate.ofEpochDay(it) }
            if (end == null) {
                PhaseSlot(i, phase, null, null)
            } else {
                val start = cursor ?: end.minusWeeks(phase.weeks.toLong()).plusDays(1)
                PhaseSlot(i, phase, start, end).also { cursor = end.plusDays(1) }
            }
        }
    }

    /**
     * 계획보다 늦은(+)·빠른(-) 날수. 지금 단계의 끝날이 지났으면 지난 날수만큼 늦고,
     * 지금 단계의 계획상 시작날이 아직 오지 않았으면(앞 단계를 일찍 통과) 그만큼 빠릅니다.
     */
    private fun shiftDays(currentIndex: Int, dues: List<Long?>, today: LocalDate): Long {
        val day = today.toEpochDay()
        val due = dues.getOrNull(currentIndex) ?: return 0
        if (due < day) return day - due
        val previousDue = (currentIndex - 1 downTo 0).firstNotNullOfOrNull { dues[it] } ?: return 0
        val plannedStart = previousDue + 1
        return if (plannedStart > day) -(plannedStart - day) else 0
    }

    private const val SCHOOL_YEAR_START_MONTH = 3
    private const val MONTHS_PER_YEAR = 12
    /** 초1 3월 입학 때의 대략적인 만 나이(6세 6개월). */
    private const val FIRST_GRADE_MONTHS = 78
}
