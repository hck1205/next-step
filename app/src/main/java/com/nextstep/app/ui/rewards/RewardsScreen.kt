package com.nextstep.app.ui.rewards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.reward.RewardKind
import com.nextstep.app.domain.reward.RewardView
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.common.UiDefaults
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.card.GameCard
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.dialog.PromiseRewardDialog
import com.nextstep.app.ui.components.row.RewardRow
import com.nextstep.app.ui.rewards.components.BadgeGrid
import com.nextstep.app.ui.rewards.components.XpBreakdownCard

/**
 * 기록 › 목표·할 일 › 보상·배지. 게임 요소가 켜져 있으면 레벨·이번 주 도전·배지판·경험치 내역,
 * 그 아래 보상(받을 차례 → 약속 → 받은 것). 학부모·멘토는 목표·레벨에 보상을 약속하고, 이루면 "줬어요"로 남깁니다.
 * 둘 다 선택이라, 꺼 두거나 약속하지 않아도 다른 화면은 그대로입니다.
 */
@Composable
fun RewardsScreen(caps: Capabilities, showsNumbers: Boolean, actions: RewardsActions, viewModel: RewardsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RewardsContent(state = state, caps = caps, showsNumbers = showsNumbers, actions = actions, onEvent = viewModel::onEvent)
}

@Composable
internal fun RewardsContent(state: RewardsUiState, caps: Capabilities, showsNumbers: Boolean, actions: RewardsActions, onEvent: (RewardsEvent) -> Unit) {
    var promising by remember { mutableStateOf(false) }
    val open: (RewardView) -> (() -> Unit)? = { v -> if (v.kind == RewardKind.GOAL) ({ actions.onOpenGoal(v.reward.targetId) }) else null }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.gamify) {
                item { GameCard(state.profile, state.nextReward, showsNumbers = showsNumbers) }
                item { SectionTitle("배지 ${state.profile.earnedBadges.size}/${state.profile.badges.size}") }
                item { BadgeGrid(state.profile.badges, showsNumbers) }
                if (showsNumbers) item { XpBreakdownCard(state.profile.lines, state.profile.xp) }
            } else if (caps.canToggleGamification) {
                item { AppCard { EmptyState("레벨·배지는 꺼져 있어요. 설정 › 레벨·배지에서 켤 수 있어요") } }
            }
            item { SectionTitle("보상") }
            if (state.loaded && state.rewards.isEmpty()) {
                item {
                    AppCard {
                        EmptyState(
                            if (caps.canGiveRewards) "목표나 레벨에 작은 보상을 약속해 보세요. 보상은 꼭 하지 않아도 돼요."
                            else "아직 약속된 보상이 없어요",
                        )
                    }
                }
            }
            listOf(state.due, state.promised, state.given.take(UiDefaults.MAX_ROWS)).filter { it.isNotEmpty() }.forEach { group ->
                item(key = "rw-" + group.first().status.name) {
                    AppCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            group.forEach { v ->
                                RewardRow(
                                    v, onOpen = open(v),
                                    onGive = if (caps.canGiveRewards) ({ onEvent(RewardsEvent.Give(v.reward.id)) }) else null,
                                    onCancel = if (caps.canGiveRewards) ({ onEvent(RewardsEvent.Cancel(v.reward.id)) }) else null,
                                )
                            }
                        }
                    }
                }
            }
        }
        if (caps.canGiveRewards && state.canPromise) {
            ExtendedFloatingActionButton(
                onClick = { promising = true }, icon = { Icon(Icons.Default.CardGiftcard, contentDescription = null) }, text = { Text("보상 약속하기") },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 96.dp),
            )
        }
    }
    if (promising) {
        PromiseRewardDialog(
            goals = state.goals, levels = state.levelChoices, onDismiss = { promising = false },
            onSave = { kind, target, title -> onEvent(RewardsEvent.Promise(kind, target, title)) },
        )
    }
}
