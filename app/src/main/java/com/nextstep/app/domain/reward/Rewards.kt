package com.nextstep.app.domain.reward

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.RewardEntity
import com.nextstep.app.data.model.GoalStatus

/**
 * 보상 계산. 순수 함수입니다. 보상은 선택 사항이고, 약속·주기는 학부모·멘토가 합니다.
 * 과한 외적 보상이 스스로 하려는 마음을 덮지 않도록, 보상은 할 일 하나마다가 아니라 목표·레벨처럼 큰 마디에만 겁니다.
 */
object Rewards {
    /** 받을 차례 → 약속 → 받음 순서(받음은 최근 준 것부터). 대상 목표가 지워졌으면 뺍니다. */
    fun views(rewards: List<RewardEntity>, goals: List<GoalEntity>, level: Int): List<RewardView> {
        val byId = goals.filter { !it.deleted }.associateBy { it.id }
        return rewards.filter { !it.deleted }.mapNotNull { r ->
            val kind = RewardKind.entries.firstOrNull { it.name == r.kind } ?: return@mapNotNull null
            val (reached, target) = when (kind) {
                RewardKind.GOAL -> {
                    val goal = byId[r.targetId] ?: return@mapNotNull null
                    (goal.status == GoalStatus.DONE) to goal.title
                }
                RewardKind.LEVEL -> {
                    val n = r.targetId.toIntOrNull() ?: return@mapNotNull null
                    (level >= n) to "레벨 $n"
                }
            }
            val status = when {
                r.givenAt != null -> RewardStatus.GIVEN
                reached -> RewardStatus.EARNED
                else -> RewardStatus.PROMISED
            }
            RewardView(r, kind, status, target)
        }.sortedWith(compareBy<RewardView> { it.status.ordinal }.thenByDescending { it.reward.givenAt ?: it.reward.createdAt })
    }

    fun due(views: List<RewardView>): List<RewardView> = views.filter { it.status == RewardStatus.EARNED }

    /** 이 목표에 걸린 보상(아직 안 준 것이 먼저). */
    fun forGoal(views: List<RewardView>, goalId: String): RewardView? =
        views.filter { it.kind == RewardKind.GOAL && it.reward.targetId == goalId }.minByOrNull { it.status.ordinal }

    /** 아이에게 보여 줄 다음 보상 한 줄: 받을 차례가 먼저, 다음은 가장 가까운 레벨 약속, 그다음 목표 약속. */
    fun next(views: List<RewardView>): RewardView? =
        views.filter { it.status != RewardStatus.GIVEN }.minWithOrNull(
            compareBy<RewardView> { it.status.ordinal }.thenBy { it.kind != RewardKind.LEVEL }.thenBy { it.reward.targetId.toIntOrNull() ?: Int.MAX_VALUE },
        )

    /** 목표마다 아직 주지 않은 보상 이름(목표 카드의 한 줄). */
    fun openByGoal(views: List<RewardView>): Map<String, String> =
        views.filter { it.kind == RewardKind.GOAL && it.status != RewardStatus.GIVEN }.associate { it.reward.targetId to it.reward.title }

    /** 약속할 수 있는 레벨: 지금 레벨 다음부터 [LEVEL_CHOICES]개. */
    fun levelChoices(current: Int): List<Int> = (current + 1..current + LEVEL_CHOICES).toList()

    /** 약속 창의 예시: 물건보다 함께하는 시간·고르는 권리. */
    val IDEAS: List<String> = listOf("주말에 같이 보드게임", "저녁 메뉴 고르기", "같이 영화 보기", "가고 싶은 곳 나들이")

    private const val LEVEL_CHOICES = 5
}
