package com.nextstep.app.ui.roadmap

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.local.RoadmapItemEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.domain.Capabilities
import com.nextstep.app.domain.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.DateField
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.LabeledProgress
import com.nextstep.app.ui.components.SectionTitle
import com.nextstep.app.ui.components.SubjectPicker
import com.nextstep.app.ui.components.SubjectTag
import java.time.LocalDate

/**
 * 학습 로드맵. 멘토(또는 학부모 겸 멘토)가 큐레이팅하고, 학생이 진행 상태를 갱신하고, 학부모는 진행률을 봅니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadmapScreen(caps: Capabilities, onBack: (() -> Unit)?, viewModel: RoadmapViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showEdit by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<RoadmapItemEntity?>(null) }
    var showDone by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (caps.isStudent) "내 학습 로드맵" else "${state.studentName.ifBlank { "학생" }} 학습 로드맵") },
                navigationIcon = { if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } },
            )
        },
        floatingActionButton = {
            if (caps.canEditRoadmap) FloatingActionButton(onClick = { editing = null; showEdit = true }) { Icon(Icons.Default.Add, contentDescription = "로드맵 항목 추가") }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                AppCard {
                    Column {
                        LabeledProgress("전체 진행률", state.completion, MaterialTheme.colorScheme.primary, trailing = "${state.done.size}/${state.items.size} 완료")
                        Spacer(Modifier.height(6.dp))
                        Text(
                            when {
                                caps.canEditRoadmap -> "학생이 어떤 순서로, 어떤 자료로, 언제까지 공부할지 큐레이팅하세요. 학생 홈과 학습 계획에 그대로 반영됩니다."
                                caps.isStudent -> "멘토가 제안한 순서예요. 시작하면 '진행 중', 끝내면 '완료'로 바꿔 주세요."
                                else -> "멘토가 제안한 학습 순서와 자녀의 진행 상황이에요."
                            },
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (caps.canEditRoadmap && state.suggestions.isNotEmpty()) {
                item { SectionTitle("진도 기반 추천 (눌러서 추가)") }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.suggestions.take(6).forEach { (subject, title) ->
                            AssistChip(
                                onClick = { viewModel.addSuggestion(subject, title) },
                                label = { Text("${subject.name} · $title") },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                            )
                        }
                    }
                }
            }

            item { SectionTitle("진행 중 · 예정") }
            if (state.active.isEmpty()) item { AppCard { EmptyState(if (caps.canEditRoadmap) "첫 로드맵 항목을 추가해 보세요" else "아직 제안된 로드맵이 없어요") } }
            items(state.active, key = { it.id }) { item ->
                RoadmapRow(item, state.subjects, caps,
                    onStatus = { viewModel.setStatus(item.id, it) },
                    onEdit = { editing = item; showEdit = true })
            }

            if (state.done.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("완료 ${state.done.size}개", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Switch(checked = showDone, onCheckedChange = { showDone = it })
                    }
                }
                if (showDone) items(state.done, key = { it.id }) { item ->
                    RoadmapRow(item, state.subjects, caps,
                        onStatus = { viewModel.setStatus(item.id, it) },
                        onEdit = { editing = item; showEdit = true })
                }
            }
        }
    }

    if (showEdit) {
        RoadmapEditDialog(editing, state.subjects, onDismiss = { showEdit = false }, onDelete = editing?.let { e -> { viewModel.delete(e.id) } }) { subjectId, title, desc, res, date ->
            viewModel.save(editing, subjectId, title, desc, res, date)
        }
    }
}

@Composable
private fun RoadmapRow(item: RoadmapItemEntity, subjects: List<SubjectEntity>, caps: Capabilities, onStatus: (RoadmapStatus) -> Unit, onEdit: () -> Unit) {
    val subject = subjects.firstOrNull { it.id == item.subjectId }
    val done = item.status == RoadmapStatus.DONE
    val overdue = !done && item.targetDate != null && item.targetDate < DateUtils.today().toEpochDay()
    AppCard(onClick = if (caps.canEditRoadmap) onEdit else null) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (done) { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary); Spacer(Modifier.width(6.dp)) }
                Text(item.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), textDecoration = if (done) TextDecoration.LineThrough else null)
                Text(
                    item.status.label, style = MaterialTheme.typography.labelSmall,
                    color = when (item.status) { RoadmapStatus.PLANNED -> MaterialTheme.colorScheme.onSurfaceVariant; RoadmapStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary; RoadmapStatus.DONE -> MaterialTheme.colorScheme.secondary },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                SubjectTag(subject)
                item.targetDate?.let {
                    Text(
                        "${DateUtils.formatDate(DateUtils.fromEpochDay(it))}까지" + (if (overdue) " · 지남" else " · ${DateUtils.dDay(DateUtils.fromEpochDay(it))}"),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (item.createdByName.isNotBlank()) Text("${item.createdByName} 제안", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (item.description.isNotBlank()) Text(item.description, style = MaterialTheme.typography.bodySmall)
            if (item.resource.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    Text(item.resource, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            if (caps.canUpdateRoadmapProgress && !done) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RoadmapStatus.entries.forEach { s ->
                        FilterChip(selected = item.status == s, onClick = { onStatus(s) }, label = { Text(s.label, style = MaterialTheme.typography.labelSmall) })
                    }
                }
            } else if (caps.canUpdateRoadmapProgress && done) {
                TextButton(onClick = { onStatus(RoadmapStatus.IN_PROGRESS) }) { Text("다시 진행 중으로") }
            }
        }
    }
}

@Composable
private fun RoadmapEditDialog(
    existing: RoadmapItemEntity?,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: (String?, String, String, String, LocalDate?) -> Unit,
) {
    var subjectId by remember { mutableStateOf(existing?.subjectId) }
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var desc by remember { mutableStateOf(existing?.description ?: "") }
    var res by remember { mutableStateOf(existing?.resource ?: "") }
    var hasDate by remember { mutableStateOf(existing?.targetDate != null) }
    var date by remember { mutableStateOf(existing?.targetDate?.let { DateUtils.fromEpochDay(it) } ?: LocalDate.now().plusDays(7)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "로드맵 항목 추가" else "로드맵 항목 편집") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("무엇을 (예: 일차방정식 개념 정리)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                SubjectPicker(subjects, subjectId, onSelect = { subjectId = it })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("어떻게 (방법·범위·주의점)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = res, onValueChange = { res = it }, label = { Text("자료 (교재명, 강의, 링크)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("목표일 설정", modifier = Modifier.weight(1f))
                    Switch(checked = hasDate, onCheckedChange = { hasDate = it })
                }
                if (hasDate) DateField("목표일", date, onChange = { date = it })
            }
        },
        confirmButton = { TextButton(enabled = title.isNotBlank(), onClick = { onSave(subjectId, title.trim(), desc.trim(), res.trim(), if (hasDate) date else null); onDismiss() }) { Text("저장") } },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = { onDelete(); onDismiss() }) { Text("삭제", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("취소") }
            }
        },
    )
}
