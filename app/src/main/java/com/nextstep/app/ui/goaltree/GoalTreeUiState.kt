package com.nextstep.app.ui.goaltree

import com.nextstep.app.domain.goaltree.GoalNode
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.domain.journey.GoalArea

/**
 * 기록 › 목표·할 일 › 목표: 사람이 만든 목표 트리. [roots] 는 거르개에 맞는 맨 위 목표, [childrenOf] 로 아래 목표를 펼칩니다.
 */
data class GoalTreeUiState(
    val loaded: Boolean = false,
    val nodes: List<GoalNode> = emptyList(),
    val filter: GoalFilter = GoalFilter.ACTIVE,
    val area: GoalArea? = null,
    /** 목표마다 아직 주지 않은 보상 이름(카드의 선물 한 줄). */
    val rewardTitles: Map<String, String> = emptyMap(),
) {
    private val shown: List<GoalNode> get() = nodes.filter { it.goal.status == filter.status && (area == null || it.goal.area == area.name) }
    /** 거르개에 맞는 목표 중, 이어지는 목표가 같은 거르개 안에 없는 것. */
    val roots: List<GoalNode> get() = GoalTree.roots(shown).sortedWith(compareBy<GoalNode> { it.daysLeft ?: Int.MAX_VALUE }.thenByDescending { it.goal.createdAt })
    fun childrenOf(goalId: String): List<GoalNode> = shown.filter { it.goal.leadsTo == goalId }
    val areas: List<GoalArea> get() = GoalArea.entries.filter { a -> nodes.any { it.goal.area == a.name } }
    fun count(f: GoalFilter): Int = nodes.count { it.goal.status == f.status }
}
