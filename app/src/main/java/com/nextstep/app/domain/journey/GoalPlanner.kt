package com.nextstep.app.domain.journey

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.MilestoneStatus
import java.time.LocalDate

/**
 * 목표를 구간별 단계로 쪼개고, 진행률과 "이번 구간에 할 단계"를 계산합니다. 순수 함수입니다.
 */
object GoalPlanner {

    /**
     * 트랙을 아이의 달력에 맞춰 단계로 만듭니다. 달력에 없는 구간의 단계는 건너뛰고,
     * 이미 지난 구간의 단계는 그대로 두되(기록용) 화면에서 "지난 구간"으로 보입니다.
     */
    fun stepsFor(track: GoalTrack, periods: List<JourneyPeriod>, goalId: String, familyId: String): List<GoalStepEntity> {
        val keys = periods.map { it.key }.toSet()
        return track.steps.filter { it.periodKey in keys }.mapIndexed { index, step ->
            GoalStepEntity(familyId = familyId, goalId = goalId, periodKey = step.periodKey, orderIndex = index, title = step.title, detail = step.detail)
        }
    }

    /** 트랙이 아이의 지금 구간과 앞으로의 구간에 걸쳐 있으면 추천. 이미 다 지난 트랙은 제외. */
    fun relevantTracks(tracks: List<GoalTrack>, periods: List<JourneyPeriod>, currentKey: String?): List<GoalTrack> {
        val index = periods.indexOfFirst { it.key == currentKey }
        val upcomingKeys = (if (index < 0) periods else periods.drop(index)).map { it.key }.toSet()
        return tracks.filter { t -> t.steps.any { it.periodKey in upcomingKeys } }
    }

    fun progress(steps: List<GoalStepEntity>): Float {
        val live = steps.filter { !it.deleted && it.status != MilestoneStatus.SKIPPED }
        if (live.isEmpty()) return 0f
        return live.count { it.status == MilestoneStatus.DONE }.toFloat() / live.size
    }

    /** 이번 구간의 단계 + 아직 안 끝난 지난 구간 단계(밀린 것). */
    fun currentSteps(steps: List<GoalStepEntity>, periods: List<JourneyPeriod>, currentKey: String?): List<GoalStepEntity> {
        val order = periods.withIndex().associate { it.value.key to it.index }
        val now = order[currentKey] ?: return emptyList()
        return steps.filter { !it.deleted && (order[it.periodKey] ?: Int.MAX_VALUE) <= now && it.status != MilestoneStatus.DONE && it.status != MilestoneStatus.SKIPPED }
            .sortedWith(compareBy<GoalStepEntity> { order[it.periodKey] }.thenBy { it.orderIndex })
    }

    /** 목표가 저장된 단계를 모두 끝냈는지. */
    fun isComplete(steps: List<GoalStepEntity>): Boolean =
        steps.any { !it.deleted } && steps.filter { !it.deleted }.all { it.status == MilestoneStatus.DONE || it.status == MilestoneStatus.SKIPPED }

    fun stepsOf(goal: GoalEntity, steps: List<GoalStepEntity>): List<GoalStepEntity> =
        steps.filter { it.goalId == goal.id && !it.deleted }.sortedBy { it.orderIndex }

    /**
     * 단계를 할 일로 바꿉니다. 마감은 단계에 날짜가 있으면 그 날(지났으면 오늘), 없으면 단계 구간의 끝이고,
     * 구간이 이미 지났거나 없으면 오늘 + [FALLBACK_DUE_DAYS]일입니다.
     * 메모에 목표 제목과 단계 설명을 남겨 할 일 목록에서도 맥락이 보이게 합니다.
     */
    fun taskFor(step: GoalStepEntity, goalTitle: String, period: JourneyPeriod?, today: LocalDate, createdByRole: String, type: TaskType = TaskType.OTHER): TaskEntity {
        val stepDue = step.dueDate?.let { LocalDate.ofEpochDay(it) }?.let { if (it.isBefore(today)) today else it }
        val due = stepDue ?: period?.end?.takeIf { !it.isBefore(today) } ?: today.plusDays(FALLBACK_DUE_DAYS)
        return TaskEntity(
            familyId = "", title = step.title, type = type, dueDate = due.toEpochDay(), createdByRole = createdByRole,
            note = listOf(goalTitle, step.detail).filter { it.isNotBlank() }.joinToString(" · "),
        )
    }

    private const val FALLBACK_DUE_DAYS = 7L
}
