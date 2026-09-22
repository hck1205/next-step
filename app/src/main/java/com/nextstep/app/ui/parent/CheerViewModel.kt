package com.nextstep.app.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.NoteRepository
import com.nextstep.app.domain.stats.StudyStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CheerViewModel(
    private val streams: FamilyDataStreams,
    private val notes: NoteRepository,
) : ViewModel() {

    val state: StateFlow<CheerUiState> = combine(streams.profile, streams.sessions, streams.tasks, streams.topics, combine(streams.notes, streams.subjects) { n, s -> n to s }) { profile, sessions, tasks, topics, (notes, subjects) ->
        val todayStart = com.nextstep.app.domain.time.DateUtils.startOfDayMillis(com.nextstep.app.domain.time.DateUtils.today())
        val todayMinutes = StudyStats.todayMinutes(sessions)
        val streak = StudyStats.studyStreak(sessions)
        val doneToday = tasks.count { it.done && it.updatedAt >= todayStart }
        val reviewedToday = topics.count { it.updatedAt >= todayStart && it.status.order >= com.nextstep.app.data.model.TopicStatus.REVIEWED.order }
        val suggestions = buildList {
            if (todayMinutes >= 60) add("오늘 ${com.nextstep.app.domain.time.DateUtils.formatMinutes(todayMinutes)} 공부한 거 봤어. 정말 대단해!")
            if (streak >= 3) add("${streak}일 연속으로 공부했네. 꾸준함이 최고야 👏")
            if (doneToday > 0) add("오늘 할 일 ${doneToday}개 끝낸 거 멋지다!")
            if (reviewedToday > 0) add("복습까지 챙기다니, 배운 걸 내 것으로 만들고 있구나.")
            val topSubject = subjects.maxByOrNull { s -> sessions.filter { it.subjectId == s.id && it.startAt >= todayStart }.sumOf { it.durationMinutes } }
            if (topSubject != null && todayMinutes > 0) add("${topSubject.name} 열심히 하는 모습 보기 좋아.")
            if (isEmpty()) add("오늘도 수고했어. 네 속도대로 가면 돼 🙂")
            add("힘들면 언제든 얘기해. 응원하고 있어.")
        }
        CheerUiState(
            studentName = profile.studentName, myName = profile.displayName, streak = streak,
            todayMinutes = todayMinutes, todayDoneTasks = doneToday, todayTopicsReviewed = reviewedToday,
            weekMinutes = StudyStats.weekMinutes(sessions), daily = StudyStats.dailyMinutes(sessions, 7),
            subjects = subjects, notes = notes, cheerSuggestions = suggestions,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CheerUiState())

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
