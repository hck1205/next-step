package com.nextstep.app.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.ColorDot
import com.nextstep.app.ui.components.card.EmptyCard
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.card.subjectColor
import com.nextstep.app.ui.components.dialog.SubjectEditDialog
import com.nextstep.app.ui.components.dialog.TextInputDialog
import com.nextstep.app.ui.components.layout.BackButton
import com.nextstep.app.ui.progress.components.ClassProgressDialog
import com.nextstep.app.ui.progress.components.ProgressSummaryCard
import com.nextstep.app.ui.progress.components.QueueHintCard
import com.nextstep.app.ui.progress.components.TopicRow

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
        topBar = { SubjectTopBar(subject?.name.orEmpty(), color, onBack = actions.onBack, onEdit = if (caps.canEditSubjects) ({ showEdit = true }) else null) },
        floatingActionButton = { if (caps.canEditTopics) FloatingActionButton(onClick = { showAddTopics = true }) { Icon(Icons.Default.Add, contentDescription = "단원 추가") } },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { ProgressSummaryCard(state.topics, subject?.teacher, color, caps, onSetProgress = { showProgressPicker = true }) }

            if (state.reviewQueue.isNotEmpty() || state.previewQueue.isNotEmpty()) {
                item { QueueHintCard(state.reviewQueue, state.previewQueue, PREVIEW_HINTS) }
            }

            item { SectionTitle("단원 목록") }
            if (state.topics.isEmpty()) item { EmptyCard("단원을 추가하세요 (여러 개는 줄바꿈으로 구분)") }
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
        TextInputDialog(
            title = "단원 추가", label = "예: 1. 문자와 식\n2. 일차방정식", minLines = 3, hint = "여러 단원은 줄바꿈이나 쉼표로 구분하세요.", confirmLabel = "추가",
            onConfirm = { onEvent(SubjectDetailEvent.AddTopics(it)) }, onDismiss = { showAddTopics = false },
        )
    }
    if (showEdit && subject != null) {
        SubjectEditDialog(subject, onDismiss = { showEdit = false }) { name, c, goal, teacher ->
            onEvent(SubjectDetailEvent.UpdateSubject(subject.copy(name = name, color = c, weeklyGoalMinutes = goal, teacher = teacher)))
        }
    }
    if (showProgressPicker) {
        ClassProgressDialog(state.topics, state.classIndex, onSelect = { onEvent(SubjectDetailEvent.SetClassProgress(it)) }, onDismiss = { showProgressPicker = false })
    }
}

/** 과목 색 점과 이름. 편집할 수 있으면([onEdit]) 연필. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectTopBar(name: String, color: Color, onBack: () -> Unit, onEdit: (() -> Unit)?) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorDot(color, TITLE_DOT); Spacer(Modifier.width(8.dp)); Text(name)
            }
        },
        navigationIcon = { BackButton(onBack) },
        actions = { if (onEdit != null) IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "과목 편집") } },
    )
}

private const val TITLE_DOT = 12

/** 예습 추천 문장에 넣는 단원 수. */
private const val PREVIEW_HINTS = 2
