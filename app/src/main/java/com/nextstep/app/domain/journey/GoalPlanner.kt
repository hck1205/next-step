package com.nextstep.app.domain.journey

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.domain.text.ratioOf

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
        return ratioOf(live.count { it.status == MilestoneStatus.DONE }, live.size)
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
        steps.any { !it.deleted } && steps.filter { !it.deleted }.all { it.status.isClosed }

    fun stepsOf(goal: GoalEntity, steps: List<GoalStepEntity>): List<GoalStepEntity> =
        steps.filter { it.goalId == goal.id && !it.deleted }.sortedBy { it.orderIndex }

    /** 추천 트랙으로 시작하는 목표와, 아이의 달력에 맞춘 단계들. */
    fun fromTrack(track: GoalTrack, periods: List<JourneyPeriod>): Pair<GoalEntity, List<GoalStepEntity>> {
        val goal = GoalEntity(familyId = "", trackId = track.id, title = track.title, area = track.area.name, description = track.description)
        return goal to stepsFor(track, periods, goal.id, "")
    }

    /** 사람이 직접 만든 목표. [stepsByPeriod] 은 (구간 key, 단계 제목) 이고 빈 제목은 건너뜁니다. */
    fun custom(title: String, area: GoalArea, description: String, stepsByPeriod: List<Pair<String, String>>): Pair<GoalEntity, List<GoalStepEntity>> {
        val goal = GoalEntity(familyId = "", title = title.trim(), area = area.name, description = description.trim())
        val steps = stepsByPeriod.filter { it.second.isNotBlank() }.mapIndexed { i, (periodKey, stepTitle) -> step(goal.id, periodKey, i, stepTitle) }
        return goal to steps
    }

    /** 목표에 더하는 단계 하나. [order] 는 목표의 단계 수(맨 뒤에 붙음). */
    fun step(goalId: String, periodKey: String, order: Int, title: String): GoalStepEntity =
        GoalStepEntity(familyId = "", goalId = goalId, periodKey = periodKey, orderIndex = order, title = title.trim())
}
