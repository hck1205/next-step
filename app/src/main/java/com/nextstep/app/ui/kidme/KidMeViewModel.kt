package com.nextstep.app.ui.kidme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.stats.StickerStats
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/** 아이용 "나" 탭(스티커판). 숫자 비교 없이 모은 스티커와 한 것만 보여 줍니다. */
class KidMeViewModel(
    streams: FamilyDataStreams,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    val state: StateFlow<KidMeUiState> = combine(streams.profile, streams.sessions, streams.tasks, streams.activities) { profile, sessions, tasks, activities ->
        val day = today()
        KidMeUiState(
            studentName = profile.studentName,
            today = day,
            board = StickerStats.board(sessions, tasks, activities, day),
            recentActivities = activities.filter { !it.deleted }.sortedByDescending { it.date }.take(RECENT_ACTIVITIES),
            loaded = true,
        )
    }.asUiState(viewModelScope, KidMeUiState())

    private companion object {
        const val RECENT_ACTIVITIES = 6
    }
}
