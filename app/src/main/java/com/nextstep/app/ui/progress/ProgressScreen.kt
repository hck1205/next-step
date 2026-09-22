package com.nextstep.app.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.DateUtils
import com.nextstep.app.domain.SubjectProgress
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.ColorDot
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.LabeledProgress
import com.nextstep.app.ui.components.subjectColor
import com.nextstep.app.ui.theme.SubjectPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(role: Role, onOpenSubject: (String) -> Unit, viewModel: ProgressViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (role == Role.PARENT) "자녀 과목별 진도" else "과목별 진도") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, contentDescription = "과목 추가") }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.progress.isEmpty()) item { AppCard { EmptyState("과목을 추가하고 단원을 등록해 보세요") } }
            items(state.progress, key = { it.subject.id }) { p -> SubjectProgressCard(p, onClick = { onOpenSubject(p.subject.id) }) }
        }
    }

    if (showAdd) {
        SubjectEditDialog(null, onDismiss = { showAdd = false }) { name, color, goal, teacher ->
            viewModel.addSubject(name, color, goal, teacher)
        }
    }
}

@Composable
private fun SubjectProgressCard(p: SubjectProgress, onClick: () -> Unit) {
    val color = subjectColor(p.subject.color)
    AppCard(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorDot(color, 12)
                Spacer(Modifier.width(8.dp))
                Text(p.subject.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (p.subject.weeklyGoalMinutes > 0) Text("주 ${DateUtils.formatMinutes(p.subject.weeklyGoalMinutes)} 목표", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (p.total == 0) {
                Text("등록된 단원이 없어요. 눌러서 추가하세요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LabeledProgress("학급 진도", p.classRatio, color.copy(alpha = 0.5f), trailing = "${p.classCovered}/${p.total} 단원")
                LabeledProgress("내 복습", p.myRatio, color, trailing = "${p.reviewed}/${p.total} 단원")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QueueChip("복습 대기 ${p.reviewQueue.size + (p.classCovered - p.reviewed - p.reviewQueue.size).coerceAtLeast(0)}", MaterialTheme.colorScheme.tertiary)
                    QueueChip("예습 추천 ${p.previewQueue.size}", MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun QueueChip(text: String, color: Color) {
    Box(Modifier.background(color.copy(alpha = 0.12f), MaterialTheme.shapes.small).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

/** 과목 추가/편집 다이얼로그. */
@Composable
fun SubjectEditDialog(existing: SubjectEntity?, onDismiss: () -> Unit, onSave: (String, Long, Int, String) -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var color by remember { mutableStateOf(existing?.color ?: SubjectPalette.first()) }
    var goal by remember { mutableStateOf((existing?.weeklyGoalMinutes ?: 180).toString()) }
    var teacher by remember { mutableStateOf(existing?.teacher ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "과목 추가" else "과목 편집") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("과목명") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = teacher, onValueChange = { teacher = it }, label = { Text("담당 선생님 (선택)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = goal, onValueChange = { goal = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("주간 목표 학습 시간(분)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(),
                )
                Text("색상", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SubjectPalette.forEach { c ->
                        val selected = c == color
                        Box(
                            Modifier.size(26.dp)
                                .border(if (selected) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                .padding(if (selected) 4.dp else 1.dp)
                                .background(subjectColor(c), CircleShape)
                                .clickable { color = c },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onSave(name.trim(), color, goal.toIntOrNull() ?: 0, teacher.trim()); onDismiss() }) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
