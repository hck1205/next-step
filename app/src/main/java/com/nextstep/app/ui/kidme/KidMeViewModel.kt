package com.nextstep.app.ui.kidme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.stats.StickerStats
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import com.nextstep.app.ui.common.gameInputs
import com.nextstep.app.domain.gamify.Gamify
import com.nextstep.app.domain.growth.StudentScreen
import com.nextstep.app.domain.reward.RewardStatus
import com.nextstep.app.domain.reward.Rewards
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/** 아이용 "나" 탭(스티커판). 숫자 비교 없이 모은 스티커와 한 것만 보여 줍니다. */
class KidMeViewModel(
    streams: FamilyDataStreams,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val base = combine(streams.profile, streams.sessions, streams.tasks, streams.activities) { profile, sessions, tasks, activities ->
        val day = today()
        KidMeUiState(
            studentName = profile.studentName,
            today = day,
            board = StickerStats.board(sessions, tasks, activities, day),
            recentActivities = activities.filter { !it.deleted }.sortedByDescending { it.date }.take(RECENT_ACTIVITIES),
            loaded = true,
        )
    }

    /** 나의 스티커판과 약속한 선물: 모양은 화면 단계(나이)가 정하고, 학부모가 꺼 두면 스티커판은 없고 선물만 남습니다. */
    val state: StateFlow<KidMeUiState> = combine(base, streams.members, streams.gameInputs(), streams.rewards) { s, members, input, list ->
        val student = members.firstOrNull { it.isStudent }
        val profile = Gamify.profile(input, s.today, style = StudentScreen.of(student, s.today).level.game)
        s.copy(
            game = profile.takeIf { student?.gamify != false },
            rewards = Rewards.views(list, input.goals, profile.level.number, profile.boards).filter { it.status != RewardStatus.GIVEN },
        )
    }.asUiState(viewModelScope, KidMeUiState())

    private companion object {
        const val RECENT_ACTIVITIES = 6
    }
}
