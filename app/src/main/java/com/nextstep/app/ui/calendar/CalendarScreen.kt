package com.nextstep.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.local.EventEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.local.TaskEntity
import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.DateUtils
import com.nextstep.app.domain.EventOccurrence
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.ColorDot
import com.nextstep.app.ui.components.DateField
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.OptionPicker
import com.nextstep.app.ui.components.SectionTitle
import com.nextstep.app.ui.components.SubjectPicker
import com.nextstep.app.ui.components.SubjectTag
import com.nextstep.app.ui.components.TimeField
import com.nextstep.app.ui.home.TaskRow
import com.nextstep.app.ui.timer.SessionRow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(role: Role, viewModel: CalendarViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var fabMenu by remember { mutableStateOf(false) }
    var editEvent by remember { mutableStateOf<EventEntity?>(null) }
    var showEvent by remember { mutableStateOf(false) }
    var editTask by remember { mutableStateOf<TaskEntity?>(null) }
    var showTask by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("캘린더") },
                actions = { TextButton(onClick = viewModel::today) { Text("오늘") } },
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { fabMenu = true }) { Icon(Icons.Default.Add, contentDescription = "추가") }
                DropdownMenu(expanded = fabMenu, onDismissRequest = { fabMenu = false }) {
                    DropdownMenuItem(text = { Text("일정 추가") }, onClick = { editEvent = null; showEvent = true; fabMenu = false })
                    DropdownMenuItem(text = { Text("할 일 추가") }, onClick = { editTask = null; showTask = true; fabMenu = false })
                }
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { MonthGrid(state, onPrev = viewModel::prevMonth, onNext = viewModel::nextMonth, onSelect = viewModel::select) }

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
                    Box(Modifier.clickable { editTask = t; showTask = true }) {
                        TaskRow(t, state.subjects, onToggle = { viewModel.toggleTask(t) }, onDelete = { viewModel.deleteTask(t.id) })
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
            onDelete = editEvent?.let { e -> { viewModel.deleteEvent(e.id) } },
        ) { title, subjectId, type, date, start, end, repeat, location, memo ->
            viewModel.saveEvent(editEvent, title, subjectId, type, date, start, end, repeat, location, memo)
        }
    }
    if (showTask) {
        TaskEditDialog(existing = editTask, subjects = state.subjects, defaultDate = state.selected, onDismiss = { showTask = false }) { title, subjectId, type, due ->
            viewModel.saveTask(editTask, title, subjectId, type, due, role.name)
        }
    }
}

@Composable
private fun MonthGrid(state: CalendarUiState, onPrev: () -> Unit, onNext: () -> Unit, onSelect: (LocalDate) -> Unit) {
    val month = state.month
    val first = month.atDay(1)
    val leading = (first.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val cells = leading + month.lengthOfMonth()
    val rows = (cells + 6) / 7
    val today = DateUtils.today()
    AppCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrev) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "이전 달") }
                Text(DateUtils.formatMonth(first), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                IconButton(onClick = onNext) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "다음 달") }
            }
            Row(Modifier.fillMaxWidth()) {
                DayOfWeek.entries.forEach { d ->
                    Text(
                        DateUtils.dayOfWeekLabel(d), style = MaterialTheme.typography.labelSmall,
                        color = when (d) { DayOfWeek.SUNDAY -> MaterialTheme.colorScheme.error; DayOfWeek.SATURDAY -> MaterialTheme.colorScheme.primary; else -> MaterialTheme.colorScheme.onSurfaceVariant },
                        modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
            for (r in 0 until rows) {
                Row(Modifier.fillMaxWidth()) {
                    for (c in 0 until 7) {
                        val idx = r * 7 + c
                        val day = idx - leading + 1
                        if (day < 1 || day > month.lengthOfMonth()) {
                            Spacer(Modifier.weight(1f).height(48.dp))
                        } else {
                            val date = month.atDay(day)
                            val marker = state.markers[date]
                            val selected = date == state.selected
                            Column(
                                Modifier.weight(1f).height(48.dp).padding(2.dp)
                                    .background(if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent, MaterialTheme.shapes.small)
                                    .clickable { onSelect(date) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    day.toString(), style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        selected -> MaterialTheme.colorScheme.onPrimary
                                        date == today -> MaterialTheme.colorScheme.primary
                                        date.dayOfWeek == DayOfWeek.SUNDAY -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                )
                                if (marker != null) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 2.dp)) {
                                        if (marker.hasExam) ColorDot(MaterialTheme.colorScheme.error, 5)
                                        else if (marker.hasEvent) ColorDot(if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary, 5)
                                        if (marker.hasTask) ColorDot(MaterialTheme.colorScheme.tertiary, 5)
                                        if (marker.studyMinutes > 0) ColorDot(MaterialTheme.colorScheme.secondary, 5)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LegendItem("일정", MaterialTheme.colorScheme.primary)
                LegendItem("시험", MaterialTheme.colorScheme.error)
                LegendItem("할 일", MaterialTheme.colorScheme.tertiary)
                LegendItem("학습 기록", MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ColorDot(color, 6); Spacer(Modifier.width(3.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CalendarEventRow(occ: EventOccurrence, subjects: List<SubjectEntity>, onClick: () -> Unit) {
    val subject = subjects.firstOrNull { it.id == occ.event.subjectId }
    val typeColor = when (occ.event.type) {
        EventType.EXAM -> MaterialTheme.colorScheme.error
        EventType.CLASS -> MaterialTheme.colorScheme.primary
        EventType.ACADEMY -> MaterialTheme.colorScheme.tertiary
        EventType.STUDY -> MaterialTheme.colorScheme.secondary
        EventType.OTHER -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    AppCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 4.dp, height = 36.dp).background(typeColor, CircleShape))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.width(52.dp)) {
                Text(DateUtils.formatTime(occ.startAt), style = MaterialTheme.typography.titleSmall)
                Text(DateUtils.formatTime(occ.endAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(occ.event.title, style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(occ.event.type.label + (if (occ.event.repeatWeekly) " · 매주" else ""), style = MaterialTheme.typography.labelSmall, color = typeColor)
                    if (subject != null) SubjectTag(subject)
                    if (occ.event.location.isNotBlank()) Text(occ.event.location, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun EventEditDialog(
    existing: EventEntity?,
    subjects: List<SubjectEntity>,
    defaultDate: LocalDate,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: (String, String?, EventType, LocalDate, LocalTime, LocalTime, Boolean, String, String) -> Unit,
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var subjectId by remember { mutableStateOf(existing?.subjectId) }
    var type by remember { mutableStateOf(existing?.type ?: EventType.CLASS) }
    var date by remember { mutableStateOf(existing?.let { DateUtils.toLocalDate(it.startAt) } ?: defaultDate) }
    var start by remember { mutableStateOf(existing?.let { DateUtils.toLocalDateTime(it.startAt).toLocalTime() } ?: LocalTime.of(16, 0)) }
    var end by remember { mutableStateOf(existing?.let { DateUtils.toLocalDateTime(it.endAt).toLocalTime() } ?: LocalTime.of(17, 0)) }
    var repeat by remember { mutableStateOf(existing?.repeatWeekly ?: false) }
    var location by remember { mutableStateOf(existing?.location ?: "") }
    var memo by remember { mutableStateOf(existing?.memo ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "일정 추가" else "일정 편집") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("제목") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OptionPicker(EventType.entries, type, label = { it.label }, onSelect = { type = it })
                SubjectPicker(subjects, subjectId, onSelect = { subjectId = it })
                DateField("날짜", date, onChange = { date = it })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeField("시작", start, onChange = { start = it }, modifier = Modifier.weight(1f))
                    TimeField("종료", end, onChange = { end = it }, modifier = Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("매주 반복 (시간표)", modifier = Modifier.weight(1f))
                    Switch(checked = repeat, onCheckedChange = { repeat = it })
                }
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("장소 (선택)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = memo, onValueChange = { memo = it }, label = { Text("메모 (선택)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank(), onClick = { onSave(title.trim(), subjectId, type, date, start, end, repeat, location.trim(), memo.trim()); onDismiss() }) { Text("저장") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = { onDelete(); onDismiss() }) { Text("삭제", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("취소") }
            }
        },
    )
}

@Composable
fun TaskEditDialog(existing: TaskEntity?, subjects: List<SubjectEntity>, defaultDate: LocalDate, onDismiss: () -> Unit, onSave: (String, String?, TaskType, LocalDate) -> Unit) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var subjectId by remember { mutableStateOf(existing?.subjectId) }
    var type by remember { mutableStateOf(existing?.type ?: TaskType.HOMEWORK) }
    var due by remember { mutableStateOf(existing?.let { DateUtils.fromEpochDay(it.dueDate) } ?: defaultDate) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "할 일 추가" else "할 일 편집") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("할 일") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OptionPicker(TaskType.entries, type, label = { it.label }, onSelect = { type = it })
                SubjectPicker(subjects, subjectId, onSelect = { subjectId = it })
                DateField("마감", due, onChange = { due = it })
            }
        },
        confirmButton = { TextButton(enabled = title.isNotBlank(), onClick = { onSave(title.trim(), subjectId, type, due); onDismiss() }) { Text("저장") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
