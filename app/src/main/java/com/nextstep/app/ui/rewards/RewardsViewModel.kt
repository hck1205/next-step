package com.nextstep.app.ui.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.RewardRepository
import com.nextstep.app.domain.gamify.Gamify
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.domain.growth.StudentScreen
import com.nextstep.app.domain.reward.Rewards
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import com.nextstep.app.ui.common.gameInputs
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 기록 › 목표·할 일 › 보상·배지. 아이 나이에 맞춘 모양으로 스티커판·레벨·배지를 기록에서 계산하고, 보상을 약속·주기·취소합니다. */
class RewardsViewModel(
    streams: FamilyDataStreams,
    private val rewards: RewardRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {

    val state: StateFlow<RewardsUiState> = combine(streams.members, streams.gameInputs(), streams.rewards) { members, input, list ->
        val day = today()
        val student = members.firstOrNull { it.isStudent }
        val style = StudentScreen.of(student, day).level.game
        val profile = Gamify.profile(input, day, style = style)
        RewardsUiState(
            loaded = true, gamify = student?.gamify ?: true, style = style, profile = profile,
            rewards = Rewards.views(list, input.goals, profile.level.number, profile.boards),
            goals = GoalTree.treeGoals(input.goals).filter { it.status == GoalStatus.ACTIVE }.sortedByDescending { it.createdAt },
        )
    }.asUiState(viewModelScope, RewardsUiState())

    fun onEvent(event: RewardsEvent) {
        when (event) {
            is RewardsEvent.Promise -> viewModelScope.launch { rewards.promise(event.kind.name, event.targetId, event.title) }
            is RewardsEvent.Give -> viewModelScope.launch { rewards.give(event.id) }
            is RewardsEvent.Cancel -> viewModelScope.launch { rewards.cancel(event.id) }
        }
    }
}
