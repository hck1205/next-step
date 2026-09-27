package com.nextstep.app.ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.calendar.components.AddMenuFab
import com.nextstep.app.ui.calendar.components.CalendarDialog
import com.nextstep.app.ui.calendar.components.CalendarDialogs
import com.nextstep.app.ui.calendar.components.CalendarEventRow
import com.nextstep.app.ui.calendar.components.MonthGrid
import com.nextstep.app.ui.components.card.EmptyCard
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.layout.hostedSectionAdd
import com.nextstep.app.ui.components.row.SessionRow
import com.nextstep.app.ui.components.row.TaskRow

@Composable
fun CalendarScreen(caps: Capabilities, viewModel: CalendarViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CalendarContent(state = state, caps = caps, onEvent = viewModel::onEvent)
}

/** 달력: 한 달 칸 → 고른 날의 일정 · 할 일 · 학습 기록. */
@Composable
internal fun CalendarContent(state: CalendarUiState, caps: Capabilities, onEvent: (CalendarEvent) -> Unit) {
    var dialog by remember { mutableStateOf<CalendarDialog?>(null) }
    val open: (CalendarDialog) -> Unit = { dialog = it }
    // 기록 탭 안에서는 "일정 추가"가 상단 바로(할 일은 가운데 기록하기로), 따로 열었을 때만 + 메뉴를 그립니다.
    val hosted = hostedSectionAdd("일정 추가") { open(CalendarDialog.EditEvent(null)) }
    Scaffold(
        floatingActionButton = {
            if (!hosted) AddMenuFab(
                taskLabel = if (caps.canCreateTasks) (if (caps.isStudent) "할 일 추가" else "과제 배정") else null,
                onAddEvent = { open(CalendarDialog.EditEvent(null)) }, onAddTask = { open(CalendarDialog.EditTask(null)) },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { MonthGrid(state, onPrev = { onEvent(CalendarEvent.PrevMonth) }, onNext = { onEvent(CalendarEvent.NextMonth) }, onSelect = { onEvent(CalendarEvent.Select(it)) }) }
            item {
                SectionTitle(
                    DateUtils.formatFullDate(state.selected) + if (state.dayMinutes > 0) " · 학습 ${DateUtils.formatMinutes(state.dayMinutes)}" else "",
                    action = { TextButton(onClick = { onEvent(CalendarEvent.Today) }) { Text("오늘") } },
                )
            }
            dayDetails(state, caps, onEvent, open)
        }
    }
    CalendarDialogs(dialog, state, caps, onEvent, onDismiss = { dialog = null })
}

/** 고른 날의 일정(누르면 고치기) · 할 일(만들 수 있으면 누르면 고치기) · 학습 기록. */
private fun LazyListScope.dayDetails(state: CalendarUiState, caps: Capabilities, onEvent: (CalendarEvent) -> Unit, open: (CalendarDialog) -> Unit) {
    if (state.dayEvents.isEmpty()) item { EmptyCard("일정이 없어요") }
    items(state.dayEvents, key = { "e" + it.event.id + it.startAt }) { occ ->
        CalendarEventRow(occ, state.subjects, onClick = { open(CalendarDialog.EditEvent(occ.event)) })
    }
    if (state.dayTasks.isNotEmpty()) {
        item { SectionTitle("할 일") }
        items(state.dayTasks, key = { "t" + it.id }) { t ->
            Box(Modifier.clickable(enabled = caps.canCreateTasks) { open(CalendarDialog.EditTask(t)) }) {
                TaskRow(t, state.subjects, onToggle = { if (caps.canCompleteTasks) onEvent(CalendarEvent.ToggleTask(t)) }, onDelete = if (caps.canCreateTasks) { { onEvent(CalendarEvent.DeleteTask(t.id)) } } else null)
            }
        }
    }
    if (state.daySessions.isNotEmpty()) {
        item { SectionTitle("학습 기록") }
        items(state.daySessions, key = { "s" + it.id }) { s -> SessionRow(s, state.subjects) }
    }
}
