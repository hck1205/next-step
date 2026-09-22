package com.nextstep.app.ui.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.local.StudySessionEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.domain.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.DateField
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.SectionTitle
import com.nextstep.app.ui.components.SubjectPicker
import com.nextstep.app.ui.components.SubjectTag
import com.nextstep.app.ui.components.TimeField
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(onBack: () -> Unit, viewModel: TimerViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showManual by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("학습 타이머") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } },
                actions = { TextButton(onClick = { showManual = true }) { Text("직접 기록") } },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                AppCard {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            DateUtils.formatElapsed(state.elapsedSeconds),
                            fontSize = 56.sp, fontWeight = FontWeight.Bold,
                            color = if (state.running != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(12.dp))
                        if (state.running == null) {
                            SubjectPicker(state.subjects, state.selectedSubjectId, onSelect = viewModel::selectSubject)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = viewModel::start, modifier = Modifier.fillMaxWidth()) { Text("공부 시작") }
                        } else {
                            val subject = state.subjects.firstOrNull { it.id == state.running?.subjectId }
                            SubjectTag(subject)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = viewModel::cancel, modifier = Modifier.weight(1f)) { Text("취소") }
                                Button(onClick = viewModel::stop, modifier = Modifier.weight(2f)) { Text("종료하고 저장") }
                            }
                        }
                        state.lastSaved?.let {
                            Spacer(Modifier.height(8.dp))
                            Text("${DateUtils.formatMinutes(it.durationMinutes)} 저장됨", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
            item { SectionTitle("오늘 기록 · 총 ${DateUtils.formatMinutes(state.todayMinutes)}") }
            if (state.todaySessions.isEmpty()) item { AppCard { EmptyState("아직 오늘 기록이 없어요") } }
            else items(state.todaySessions, key = { it.id }) { s -> SessionRow(s, state.subjects, onDelete = { viewModel.delete(s.id) }) }
        }
    }

    if (showManual) {
        ManualSessionDialog(state.subjects, onDismiss = { showManual = false }) { subjectId, date, start, minutes, note ->
            viewModel.addManual(subjectId, date, start, minutes, note)
        }
    }
}

@Composable
fun SessionRow(s: StudySessionEntity, subjects: List<SubjectEntity>, onDelete: (() -> Unit)? = null) {
    val subject = subjects.firstOrNull { it.id == s.subjectId }
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SubjectTag(subject)
                    Spacer(Modifier.width(8.dp))
                    Text(DateUtils.formatMinutes(s.durationMinutes), style = MaterialTheme.typography.titleSmall)
                }
                Text(
                    "${DateUtils.formatDate(DateUtils.toLocalDate(s.startAt))} ${DateUtils.formatTime(s.startAt)} ~ ${DateUtils.formatTime(s.endAt)}" +
                        (if (s.note.isNotBlank()) " · ${s.note}" else ""),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (onDelete != null) TextButton(onClick = onDelete) { Text("삭제") }
        }
    }
}

@Composable
fun ManualSessionDialog(subjects: List<SubjectEntity>, onDismiss: () -> Unit, onSave: (String?, LocalDate, LocalTime, Int, String) -> Unit) {
    var subjectId by remember { mutableStateOf(subjects.firstOrNull()?.id) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var start by remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0).minusHours(1)) }
    var minutes by remember { mutableStateOf("60") }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("학습 기록 직접 추가") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SubjectPicker(subjects, subjectId, onSelect = { subjectId = it })
                DateField("날짜", date, onChange = { date = it })
                TimeField("시작", start, onChange = { start = it }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = minutes, onValueChange = { minutes = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("학습 시간(분)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("메모") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = (minutes.toIntOrNull() ?: 0) > 0, onClick = { onSave(subjectId, date, start, minutes.toInt(), note); onDismiss() }) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
