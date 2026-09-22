package com.nextstep.app.ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.calendar.components.CalendarEventRow
import com.nextstep.app.ui.calendar.components.MonthGrid
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.EventEditDialog
import com.nextstep.app.ui.components.SectionTitle
import com.nextstep.app.ui.components.SessionRow
import com.nextstep.app.ui.components.TaskEditDialog
import com.nextstep.app.ui.components.TaskRow

@Composable
fun CalendarScreen(caps: Capabilities, viewModel: CalendarViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CalendarContent(state = state, caps = caps, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CalendarContent(state: CalendarUiState, caps: Capabilities, onEvent: (CalendarEvent) -> Unit) {
    var fabMenu by remember { mutableStateOf(false) }
    var editEvent by remember { mutableStateOf<EventEntity?>(null) }
    var showEvent by remember { mutableStateOf(false) }
    var editTask by remember { mutableStateOf<TaskEntity?>(null) }
    var showTask by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("캘린더") },
                actions = { TextButton(onClick = { onEvent(CalendarEvent.Today) }) { Text("오늘") } },
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { fabMenu = true }) { Icon(Icons.Default.Add, contentDescription = "추가") }
                DropdownMenu(expanded = fabMenu, onDismissRequest = { fabMenu = false }) {
                    DropdownMenuItem(text = { Text("일정 추가") }, onClick = { editEvent = null; showEvent = true; fabMenu = false })
                    if (caps.canCreateTasks) DropdownMenuItem(text = { Text(if (caps.isStudent) "할 일 추가" else "과제 배정") }, onClick = { editTask = null; showTask = true; fabMenu = false })
                }
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { MonthGrid(state, onPrev = { onEvent(CalendarEvent.PrevMonth) }, onNext = { onEvent(CalendarEvent.NextMonth) }, onSelect = { onEvent(CalendarEvent.Select(it)) }) }

            item {
                SectionTitle(DateUtils.formatFullDate(state.selected) + if (state.dayMinutes > 0) " · 학습 ${DateUtils.formatMinutes(state.dayMinutes)}" else "")
            }
            if (state.dayEvents.isEmpty()) item { AppCard { EmptyState("일정이 없어요") } }
            items(state.dayEvents, key = { "e" + it.event.id + it.startAt }) { occ ->
                CalendarEventRow(occ, state.subjects, onClick = { editEvent = occ.event; showEvent = true })
            }
            if (state.dayTasks.isNotEmpty()) {
                item { SectionTitle("할 일") }
                items(state.dayTasks, key = { "t" + it.id }) { t ->
                    Box(Modifier.clickable(enabled = caps.canCreateTasks) { editTask = t; showTask = true }) {
                        TaskRow(t, state.subjects, onToggle = { if (caps.canCompleteTasks) onEvent(CalendarEvent.ToggleTask(t)) }, onDelete = if (caps.canCreateTasks) { { onEvent(CalendarEvent.DeleteTask(t.id)) } } else null)
                    }
                }
            }
            if (state.daySessions.isNotEmpty()) {
                item { SectionTitle("학습 기록") }
                items(state.daySessions, key = { "s" + it.id }) { s -> SessionRow(s, state.subjects) }
            }
        }
    }

    if (showEvent) {
        EventEditDialog(
            existing = editEvent, subjects = state.subjects, defaultDate = state.selected,
            onDismiss = { showEvent = false },
            onDelete = editEvent?.let { e -> { onEvent(CalendarEvent.DeleteEvent(e.id)) } },
        ) { title, subjectId, type, date, start, end, repeat, location, memo ->
            onEvent(CalendarEvent.SaveEvent(editEvent, title, subjectId, type, date, start, end, repeat, location, memo))
        }
    }
    if (showTask) {
        TaskEditDialog(existing = editTask, subjects = state.subjects, defaultDate = state.selected, onDismiss = { showTask = false }) { title, subjectId, type, due ->
            onEvent(CalendarEvent.SaveTask(editTask, title, subjectId, type, due, caps.actingRoleName))
        }
    }
}
