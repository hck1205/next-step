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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.local.TopicEntity
import com.nextstep.app.domain.Capabilities
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.ColorDot
import com.nextstep.app.ui.components.ConfirmDialog
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.LabeledProgress
import com.nextstep.app.ui.components.SectionTitle
import com.nextstep.app.ui.components.TextInputDialog
import com.nextstep.app.ui.components.subjectColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(caps: Capabilities, onBack: () -> Unit, viewModel: SubjectDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } },
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
                    onToggleCovered = { viewModel.setClassProgress(if (topic.classCovered) topic.orderIndex - 1 else topic.orderIndex) },
                    onStatus = { viewModel.setStatus(topic, it) },
                    onConfidence = { viewModel.setConfidence(topic, it) },
                    onRename = { viewModel.rename(topic, it) },
                    onDelete = { viewModel.delete(topic) },
                    onAddTask = { viewModel.addTask(topic, it, caps.actingRoleName) },
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
            confirmButton = { TextButton(enabled = text.isNotBlank(), onClick = { viewModel.addTopics(text); showAddTopics = false }) { Text("추가") } },
            dismissButton = { TextButton(onClick = { showAddTopics = false }) { Text("취소") } },
        )
    }
    if (showEdit && subject != null) {
        SubjectEditDialog(subject, onDismiss = { showEdit = false }) { name, c, goal, teacher ->
            viewModel.updateSubject(subject.copy(name = name, color = c, weeklyGoalMinutes = goal, teacher = teacher))
        }
    }
    if (showProgressPicker) {
        AlertDialog(
            onDismissRequest = { showProgressPicker = false },
            title = { Text("학급 진도는 어디까지?") },
            text = {
                LazyColumn {
                    item {
                        TextButton(onClick = { viewModel.setClassProgress(-1); showProgressPicker = false }, modifier = Modifier.fillMaxWidth()) { Text("아직 시작 전") }
                    }
                    items(state.topics, key = { it.id }) { t ->
                        TextButton(onClick = { viewModel.setClassProgress(t.orderIndex); showProgressPicker = false }, modifier = Modifier.fillMaxWidth()) {
                            Text((if (t.orderIndex == state.classIndex) "● " else "") + t.title)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showProgressPicker = false }) { Text("닫기") } },
        )
    }
}

@Composable
private fun TopicRow(
    topic: TopicEntity,
    caps: Capabilities,
    onToggleCovered: () -> Unit,
    onStatus: (TopicStatus) -> Unit,
    onConfidence: (Int) -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onAddTask: (TaskType) -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    var rename by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    AppCard(onClick = { expanded = !expanded }) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = topic.classCovered, onCheckedChange = { onToggleCovered() }, enabled = caps.canEditTopics)
                Column(Modifier.weight(1f)) {
                    Text(topic.title, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        topic.status.label + (if (topic.confidence > 0) " · 이해도 ${topic.confidence}%" else ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (topic.status) {
                            TopicStatus.NOT_STARTED -> MaterialTheme.colorScheme.onSurfaceVariant
                            TopicStatus.PREVIEWED, TopicStatus.IN_CLASS -> MaterialTheme.colorScheme.primary
                            TopicStatus.REVIEWED, TopicStatus.MASTERED -> MaterialTheme.colorScheme.secondary
                        },
                    )
                }
                if (caps.canCreateTasks || caps.canEditTopics) IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "메뉴") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    if (caps.canCreateTasks) {
                        DropdownMenuItem(text = { Text(if (caps.isStudent) "예습 할 일 추가" else "예습 과제 배정") }, onClick = { onAddTask(TaskType.PREVIEW); menu = false })
                        DropdownMenuItem(text = { Text(if (caps.isStudent) "복습 할 일 추가" else "복습 과제 배정") }, onClick = { onAddTask(TaskType.REVIEW); menu = false })
                    }
                    if (caps.canEditTopics) {
                        DropdownMenuItem(text = { Text("이름 변경") }, onClick = { rename = true; menu = false })
                        DropdownMenuItem(text = { Text("삭제") }, leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }, onClick = { confirmDelete = true; menu = false })
                    }
                }
            }
            if (expanded && caps.canMarkTopicStatus) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                    TopicStatus.entries.forEach { s ->
                        FilterChip(selected = topic.status == s, onClick = { onStatus(s) }, label = { Text(s.label, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("이해도 ${topic.confidence}%  (슬라이더를 놓으면 저장)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                var conf by remember(topic.confidence) { mutableStateOf(topic.confidence.toFloat()) }
                Slider(
                    value = conf,
                    onValueChange = { conf = it },
                    onValueChangeFinished = { onConfidence(conf.toInt()) },
                    valueRange = 0f..100f,
                    steps = 9,
                )
            }
        }
    }
    if (rename) TextInputDialog("단원 이름 변경", "단원명", topic.title, onConfirm = onRename, onDismiss = { rename = false })
    if (confirmDelete) ConfirmDialog("단원 삭제", "'${topic.title}' 단원을 삭제할까요?", "삭제", onConfirm = onDelete, onDismiss = { confirmDelete = false })
}
