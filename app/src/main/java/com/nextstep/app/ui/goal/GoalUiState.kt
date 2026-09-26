package com.nextstep.app.ui.goal

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.goaltree.GoalNode
import com.nextstep.app.domain.goaltree.HistoryEvent
import com.nextstep.app.domain.reward.RewardView
import com.nextstep.app.domain.reward.Rewards
import com.nextstep.app.domain.gamify.GameStyle
import com.nextstep.app.domain.selfdirection.SelfDirectionStage
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/**
 * 목표 한 개: 목표와 세부 할 일, 이어지는 목표 길(각 목표의 달성률), 이 목표로 이어지는 작은 목표, 이 목표의 기록.
 * [stage] 는 할 일 체크를 누가 할 수 있는지(자기주도 사다리)에 씁니다.
 */
data class GoalUiState(
    val loaded: Boolean = false,
    val node: GoalNode? = null,
    /** 이어지는 목표 길(가까운 것부터)과 작은 목표의 노드. 달성률을 함께 보여 줍니다. */
    val chain: List<GoalNode> = emptyList(),
    val children: List<GoalNode> = emptyList(),
    val linkTargets: List<GoalEntity> = emptyList(),
    val history: List<HistoryEvent> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    /** 이 목표에 걸린 보상(아직 안 준 것이 먼저). 없으면 null. */
    val reward: RewardView? = null,
    /** 아이 나이에 맞춘 게임 모양: 보상 약속 창의 예시·안내가 달라집니다. */
    val gameStyle: GameStyle = GameStyle.LEVELS,
    val stage: SelfDirectionStage = SelfDirectionStage.OWN,
    val today: LocalDate = DateUtils.today(),
) {
    val rewardIdeas: List<String> get() = Rewards.ideasFor(gameStyle)
    val rewardHint: String get() = Rewards.hintFor(gameStyle)
}
