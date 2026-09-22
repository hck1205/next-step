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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.ManualSessionDialog
import com.nextstep.app.ui.components.SectionTitle
import com.nextstep.app.ui.components.SessionRow
import com.nextstep.app.ui.components.SubjectPicker
import com.nextstep.app.ui.components.SubjectTag

@Composable
fun TimerScreen(actions: TimerActions, viewModel: TimerViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TimerContent(state = state, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimerContent(state: TimerUiState, actions: TimerActions, onEvent: (TimerEvent) -> Unit) {
    var showManual by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("학습 타이머") },
                navigationIcon = { IconButton(onClick = actions.onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } },
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
                            SubjectPicker(state.subjects, state.selectedSubjectId, onSelect = { onEvent(TimerEvent.SelectSubject(it)) })
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { onEvent(TimerEvent.Start) }, modifier = Modifier.fillMaxWidth()) { Text("공부 시작") }
                        } else {
                            val subject = state.subjects.firstOrNull { it.id == state.running?.subjectId }
                            SubjectTag(subject)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onEvent(TimerEvent.Cancel) }, modifier = Modifier.weight(1f)) { Text("취소") }
                                Button(onClick = { onEvent(TimerEvent.Stop) }, modifier = Modifier.weight(2f)) { Text("종료하고 저장") }
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
            else items(state.todaySessions, key = { it.id }) { s -> SessionRow(s, state.subjects, onDelete = { onEvent(TimerEvent.Delete(s.id)) }) }
        }
    }

    if (showManual) {
        ManualSessionDialog(state.subjects, onDismiss = { showManual = false }) { subjectId, date, start, minutes, note ->
            onEvent(TimerEvent.AddManual(subjectId, date, start, minutes, note))
        }
    }
}
