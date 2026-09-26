package com.nextstep.app.domain.reward

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.RewardEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.domain.gamify.GameProfile
import com.nextstep.app.domain.gamify.GameStyle

/**
 * 보상 계산. 순수 함수입니다. 보상은 선택 사항이고, 약속·주기는 학부모·멘토가 합니다.
 * 과한 외적 보상이 스스로 하려는 마음을 덮지 않도록, 보상은 할 일 하나마다가 아니라 목표·레벨·스티커판처럼 큰 마디에만 겁니다.
 * 나이에 따라 걸 수 있는 곳과 권하는 보상이 다릅니다: 어린아이는 곧 닿는 스티커판에 작고 바로, 청소년은 목표에 인정과 선택권으로.
 */
object Rewards {
    /** 받을 차례 → 약속 → 받음 순서(받음은 최근 준 것부터). 대상 목표가 지워졌으면 뺍니다. */
    fun views(rewards: List<RewardEntity>, goals: List<GoalEntity>, level: Int, boards: Int = 0): List<RewardView> {
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
                RewardKind.BOARD -> {
                    val n = r.targetId.toIntOrNull() ?: return@mapNotNull null
                    (boards >= n) to "스티커판 ${n}장"
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

    /** 아이에게 보여 줄 다음 보상 한 줄: 받을 차례가 먼저, 다음은 가장 가까운 번호(레벨·스티커판) 약속, 그다음 목표 약속. */
    fun next(views: List<RewardView>): RewardView? =
        views.filter { it.status != RewardStatus.GIVEN }.minWithOrNull(
            compareBy<RewardView> { it.status.ordinal }.thenBy { it.kind == RewardKind.GOAL }.thenBy { it.reward.targetId.toIntOrNull() ?: Int.MAX_VALUE },
        )

    /** 목표마다 아직 주지 않은 보상 이름(목표 카드의 한 줄). */
    fun openByGoal(views: List<RewardView>): Map<String, String> =
        views.filter { it.kind == RewardKind.GOAL && it.status != RewardStatus.GIVEN }.associate { it.reward.targetId to it.reward.title }

    /**
     * 이 나이에 보상을 걸 수 있는 곳. 목표는 언제나, 스티커판은 스티커판 모양(학령 전 ~ 초2), 레벨은 레벨·배지 모양(초3 ~ 초6).
     * 청소년(성장 기록)은 레벨 보상이 유치하게 느껴지므로 목표만. 게임 요소가 꺼져 있으면 목표만.
     */
    fun kindsFor(style: GameStyle, gameOn: Boolean): List<RewardKind> = when {
        !gameOn -> listOf(RewardKind.GOAL)
        style == GameStyle.STICKERS -> listOf(RewardKind.BOARD, RewardKind.GOAL)
        style == GameStyle.LEVELS -> listOf(RewardKind.GOAL, RewardKind.LEVEL)
        else -> listOf(RewardKind.GOAL)
    }

    /** 약속 창의 대상 목록: 진행 중인 목표, 다음 레벨 [LEVEL_CHOICES]개, 다음 스티커판 [BOARD_CHOICES]개(이 나이에 맞는 것만). */
    fun targets(style: GameStyle, gameOn: Boolean, profile: GameProfile, goals: List<GoalEntity>): List<RewardTarget> =
        kindsFor(style, gameOn).flatMap { kind ->
            when (kind) {
                RewardKind.GOAL -> goals.map { RewardTarget(kind, it.id, it.title) }
                RewardKind.LEVEL -> (profile.level.number + 1..profile.level.number + LEVEL_CHOICES).map { RewardTarget(kind, "$it", "레벨 $it") }
                RewardKind.BOARD -> (profile.boards + 1..profile.boards + BOARD_CHOICES).map { RewardTarget(kind, "$it", "스티커판 ${it}장") }
            }
        }

    /** 약속 창의 예시: 어릴수록 작고 바로, 클수록 선택권과 인정. 모두 물건보다 함께하는 시간이 먼저입니다. */
    fun ideasFor(style: GameStyle): List<String> = when (style) {
        GameStyle.STICKERS -> listOf("놀이터에서 30분 더 놀기", "좋아하는 책 한 권 더 읽어 주기", "같이 쿠키 굽기", "주말 공원 나들이")
        GameStyle.LEVELS -> listOf("주말에 같이 보드게임", "저녁 메뉴 고르기", "같이 영화 보기", "가고 싶은 곳 나들이")
        GameStyle.GROWTH -> listOf("주말 하루 일정 스스로 정하기", "가고 싶던 곳 같이 가기", "갖고 싶던 책·도구", "친구와 보내는 시간")
    }

    /** 약속 창의 한 줄 안내(나이에 맞는 보상 원칙). */
    fun hintFor(style: GameStyle): String = when (style) {
        GameStyle.STICKERS -> "어린아이에게는 멀리 있는 약속보다 곧 닿는 작은 보상이 좋아요. 스티커판을 채운 날 함께 놀아 주세요."
        GameStyle.LEVELS -> "할 일 하나하나가 아니라 큰 마디에만 걸어요. 물건보다 함께하는 시간이 오래 남아요."
        GameStyle.GROWTH -> "청소년에게는 물건보다 인정과 선택권이 힘이 돼요. 스스로 세운 목표를 이룬 뒤 함께 축하해 주세요."
    }

    private const val LEVEL_CHOICES = 5
    private const val BOARD_CHOICES = 3
}
