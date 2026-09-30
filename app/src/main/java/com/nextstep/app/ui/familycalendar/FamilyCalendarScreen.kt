package com.nextstep.app.ui.familycalendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.nextstep.app.domain.familycalendar.FamilyOccurrence
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.EmptyCard
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.layout.ScreenPadding
import com.nextstep.app.ui.components.layout.hostedSectionAdd
import com.nextstep.app.ui.components.row.EventRow
import com.nextstep.app.ui.components.row.FamilyEventRow
import com.nextstep.app.ui.familycalendar.components.FamilyEventSheet
import com.nextstep.app.ui.familycalendar.components.FamilyMonthGrid
import com.nextstep.app.ui.familycalendar.components.WhoFilterRow
import java.time.LocalDate

@Composable
fun FamilyCalendarScreen(caps: Capabilities, viewModel: FamilyCalendarViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FamilyCalendarContent(state = state, canEdit = caps.isFamily, onEvent = viewModel::onEvent)
}

/**
 * 가족 달력: 누구 고르기 → 한 달 칸(종류 색 점, 아이 공부 일정은 회색 점) → 고른 날의 가족 일정 · 아이 공부 일정.
 * 날짜를 누르면 고르고, 고른 날을 한 번 더 누르면 그날로 새 일정 입력창이 열립니다. 일정을 누르면 고칩니다.
 */
@Composable
internal fun FamilyCalendarContent(state: FamilyCalendarUiState, canEdit: Boolean, onEvent: (FamilyCalendarEvent) -> Unit) {
    var target by remember { mutableStateOf<FamilyEventTarget?>(null) }
    val addOn: (LocalDate) -> Unit = { date -> if (canEdit) target = FamilyEventTarget(null, date) }
    val hosted = hostedSectionAdd(if (canEdit) "가족 일정" else null) { addOn(state.selected) }
    Scaffold(
        floatingActionButton = {
            if (!hosted && canEdit) FloatingActionButton(onClick = { addOn(state.selected) }) { Icon(Icons.Default.Add, contentDescription = "가족 일정 넣기") }
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = ScreenPadding.list, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.members.size > 1) item { WhoFilterRow(state.members, state.filter) { onEvent(FamilyCalendarEvent.Filter(it)) } }
            item {
                FamilyMonthGrid(
                    state, onPrev = { onEvent(FamilyCalendarEvent.PrevMonth) }, onNext = { onEvent(FamilyCalendarEvent.NextMonth) },
                    onDay = { date -> if (date == state.selected) addOn(date) else onEvent(FamilyCalendarEvent.Select(date)) },
                )
            }
            item { SectionTitle(DateUtils.formatFullDate(state.selected), action = { TextButton(onClick = { onEvent(FamilyCalendarEvent.Today) }) { Text("오늘") } }) }
            dayList(state, canEdit, onOpen = { target = FamilyEventTarget(it.event, state.selected) })
        }
    }
    target?.let { t ->
        FamilyEventSheet(
            existing = t.existing, date = t.date, members = state.members, onDismiss = { target = null },
            onSave = { onEvent(FamilyCalendarEvent.Save(t.existing, it)) },
            onDelete = t.existing?.let { e -> { onEvent(FamilyCalendarEvent.Delete(e.id)) } },
        )
    }
}

/** 고른 날: 가족 일정(누르면 고치기) → 아이의 공부 일정(읽기만). */
private fun LazyListScope.dayList(state: FamilyCalendarUiState, canEdit: Boolean, onOpen: (FamilyOccurrence) -> Unit) {
    if (state.dayEvents.isEmpty()) item { EmptyCard(if (canEdit) "가족 일정이 없어요 · 날짜를 한 번 더 누르면 넣어요" else "가족 일정이 없어요") }
    items(state.dayEvents, key = { "f" + it.event.id + it.start }) { occ ->
        FamilyEventRow(occ, state.members, onClick = if (canEdit) ({ onOpen(occ) }) else null)
    }
    if (state.dayStudy.isNotEmpty()) {
        item { SectionTitle("${state.studentName.ifBlank { "아이" }}의 공부 일정") }
        items(state.dayStudy, key = { "s" + it.event.id + it.startAt }) { occ -> EventRow(occ, state.subjects) }
    }
}
