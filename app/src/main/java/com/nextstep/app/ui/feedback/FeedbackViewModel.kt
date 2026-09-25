package com.nextstep.app.ui.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.mentor.FeedbackLog
import com.nextstep.app.domain.mentor.MentorScope
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/** 과제·피드백 › 피드백. 쓰기는 + 시트(격려·피드백)가 맡고, 여기서는 쌓인 기록을 주별로 봅니다. */
class FeedbackViewModel(
    streams: FamilyDataStreams,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    val state: StateFlow<FeedbackUiState> = combine(streams.notes, streams.myMember) { notes, me ->
        val visible = MentorScope.notesFor(notes.filter { !it.deleted }, me)
        val weeks = FeedbackLog.weeks(visible, today())
        FeedbackUiState(
            weeks = weeks,
            thisWeekCount = weeks.firstOrNull { it.label == FeedbackLog.label(0) }?.notes?.size ?: 0,
            fromMentors = visible.count { it.authorRole == Role.MENTOR.name },
            fromFamily = visible.count { it.authorRole != Role.MENTOR.name },
            loaded = true,
        )
    }.asUiState(viewModelScope, FeedbackUiState())
}
