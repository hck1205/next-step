package com.nextstep.app.ui.progress

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.ColorDot
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.card.LabeledProgress
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.dialog.SubjectEditDialog
import com.nextstep.app.ui.components.card.subjectColor
import com.nextstep.app.ui.progress.components.TopicRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun SubjectDetailScreen(caps: Capabilities, actions: SubjectDetailActions, viewModel: SubjectDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SubjectDetailContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SubjectDetailContent(state: SubjectDetailUiState, caps: Capabilities, actions: SubjectDetailActions, onEvent: (SubjectDetailEvent) -> Unit) {
    val subject = state.subject
    var showAddTopics by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var showProgressPicker by remember { mutableStateOf(false) }
    val color = subject?.let { subjectColor(it.color) } ?: MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ColorDot(color, 12); Spacer(Modifier.width(8.dp)); Text(subject?.name ?: "")
                    }
                },
                navigationIcon = { IconButton(onClick = actions.onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } },
                actions = { if (caps.canEditSubjects) IconButton(onClick = { showEdit = true }) { Icon(Icons.Default.Edit, contentDescription = "과목 편집") } },
            )
        },
        floatingActionButton = { if (caps.canEditTopics) FloatingActionButton(onClick = { showAddTopics = true }) { Icon(Icons.Default.Add, contentDescription = "단원 추가") } },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                AppCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val total = state.topics.size
                        val covered = state.topics.count { it.classCovered }
                        val reviewed = state.topics.count { it.status.order >= TopicStatus.REVIEWED.order }
                        LabeledProgress("학급 진도", if (total == 0) 0f else covered.toFloat() / total, color.copy(alpha = 0.5f), trailing = "$covered/$total")
                        LabeledProgress("내 복습", if (total == 0) 0f else reviewed.toFloat() / total, color, trailing = "$reviewed/$total")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (caps.canEditTopics) AssistChip(onClick = { showProgressPicker = true }, label = { Text("학급 진도 설정") })
                            if (subject?.teacher?.isNotBlank() == true) Text("담당: ${subject.teacher}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterVertically))
                        }
                        Text(
                            when {
                                caps.isStudent -> "체크 = 수업에서 배운 단원(학급 진도). 단원을 눌러 예습·복습 상태와 이해도를 기록하세요."
                                caps.canEditTopics -> "체크 = 수업에서 배운 단원. 단원을 등록하고 학급 진도를 갱신하면 학생에게 복습·예습 항목이 자동으로 뜹니다."
                                else -> "체크 = 수업에서 배운 단원. 상태와 이해도는 학생이 직접 기록한 값이에요."
                            },
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (state.reviewQueue.isNotEmpty() || state.previewQueue.isNotEmpty()) {
                item {
                    AppCard {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (state.reviewQueue.isNotEmpty()) Text("복습 필요: ${state.reviewQueue.joinToString { it.title }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                            if (state.previewQueue.isNotEmpty()) Text("예습 추천: ${state.previewQueue.take(2).joinToString { it.title }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            item { SectionTitle("단원 목록") }
            if (state.topics.isEmpty()) item { AppCard { EmptyState("단원을 추가하세요 (여러 개는 줄바꿈으로 구분)") } }
            items(state.topics, key = { it.id }) { topic ->
                TopicRow(
                    topic = topic,
                    caps = caps,
                    onToggleCovered = { onEvent(SubjectDetailEvent.SetClassProgress(if (topic.classCovered) topic.orderIndex - 1 else topic.orderIndex)) },
                    onStatus = { onEvent(SubjectDetailEvent.SetStatus(topic, it)) },
                    onConfidence = { onEvent(SubjectDetailEvent.SetConfidence(topic, it)) },
                    onRename = { onEvent(SubjectDetailEvent.Rename(topic, it)) },
                    onDelete = { onEvent(SubjectDetailEvent.Delete(topic)) },
                    onAddTask = { onEvent(SubjectDetailEvent.AddTask(topic, it, caps.actingRoleName)) },
                )
            }
        }
    }

    if (showAddTopics) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddTopics = false },
            title = { Text("단원 추가") },
            text = {
                Column {
                    Text("여러 단원은 줄바꿈이나 쉼표로 구분하세요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("예: 1. 문자와 식\n2. 일차방정식") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }
            },
            confirmButton = { TextButton(enabled = text.isNotBlank(), onClick = { onEvent(SubjectDetailEvent.AddTopics(text)); showAddTopics = false }) { Text("추가") } },
            dismissButton = { TextButton(onClick = { showAddTopics = false }) { Text("취소") } },
        )
    }
    if (showEdit && subject != null) {
        SubjectEditDialog(subject, onDismiss = { showEdit = false }) { name, c, goal, teacher ->
            onEvent(SubjectDetailEvent.UpdateSubject(subject.copy(name = name, color = c, weeklyGoalMinutes = goal, teacher = teacher)))
        }
    }
    if (showProgressPicker) {
        AlertDialog(
            onDismissRequest = { showProgressPicker = false },
            title = { Text("학급 진도는 어디까지?") },
            text = {
                LazyColumn {
                    item {
                        TextButton(onClick = { onEvent(SubjectDetailEvent.SetClassProgress(-1)); showProgressPicker = false }, modifier = Modifier.fillMaxWidth()) { Text("아직 시작 전") }
                    }
                    items(state.topics, key = { it.id }) { t ->
                        TextButton(onClick = { onEvent(SubjectDetailEvent.SetClassProgress(t.orderIndex)); showProgressPicker = false }, modifier = Modifier.fillMaxWidth()) {
                            Text((if (t.orderIndex == state.classIndex) "● " else "") + t.title)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showProgressPicker = false }) { Text("닫기") } },
        )
    }
}
