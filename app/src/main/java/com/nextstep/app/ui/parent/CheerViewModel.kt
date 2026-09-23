package com.nextstep.app.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.NoteRepository
import com.nextstep.app.domain.cheer.CheerStats
import com.nextstep.app.domain.cheer.CheerSuggestions
import com.nextstep.app.domain.growth.GrowthGuide
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** 학부모 격려 화면. 오늘 한 일을 CheerStats 로 요약하고 CheerSuggestions 가 문구를 만듭니다. */
class CheerViewModel(
    private val streams: FamilyDataStreams,
    private val notes: NoteRepository,
) : ViewModel() {

    val state: StateFlow<CheerUiState> = combine(streams.profile, streams.sessions, streams.tasks, streams.topics, combine(streams.notes, streams.subjects, streams.members) { n, s, m -> Triple(n, s, m) }) { profile, sessions, tasks, topics, (notes, subjects, members) ->
        val today = DateUtils.today()
        val stage = GrowthStage.of(members, today)
        val snapshot = CheerStats.snapshot(sessions, tasks, topics, subjects, today)
        CheerUiState(
            studentName = profile.studentName, myName = profile.displayName, streak = snapshot.streak,
            todayMinutes = snapshot.todayMinutes, todayDoneTasks = snapshot.doneToday, todayTopicsReviewed = snapshot.reviewedToday,
            weekMinutes = StudyStats.weekMinutes(sessions), daily = StudyStats.dailyMinutes(sessions, 7),
            subjects = subjects, notes = notes, cheerSuggestions = CheerSuggestions.build(snapshot, stage),
            praiseStyle = stage?.let { GrowthGuide.forStage(it).praiseStyle },
        )
    }.asUiState(viewModelScope, CheerUiState())

    fun send(text: String) = viewModelScope.launch { if (text.isNotBlank()) notes.add(text) }
    fun delete(id: String) = viewModelScope.launch { notes.delete(id) }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: CheerEvent) {
        when (event) {
            is CheerEvent.Send -> send(event.text)
            is CheerEvent.Delete -> delete(event.id)
        }
    }
}
