package com.nextstep.app.ui.insights

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.DateUtils
import com.nextstep.app.domain.Insight
import com.nextstep.app.domain.InsightAction
import com.nextstep.app.domain.InsightKind
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.BarChart
import com.nextstep.app.ui.components.BarItem
import com.nextstep.app.ui.components.DonutChart
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.HourHeatStrip
import com.nextstep.app.ui.components.RadarChart
import com.nextstep.app.ui.components.SectionTitle
import com.nextstep.app.ui.components.Slice
import com.nextstep.app.ui.components.SubjectTag
import com.nextstep.app.ui.components.subjectColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(role: Role, viewModel: InsightsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showNote by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text(if (role == Role.PARENT) "자녀 학습 분석" else "학습 분석") }) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { SectionTitle("강점 · 보완점 · 제안") }
            items(state.insights) { insight ->
                InsightCard(insight, state.subjects, onAction = if (role == Role.STUDENT) { a -> viewModel.applyAction(a) } else null)
            }

            if (state.scores.size >= 3) {
                item {
                    SectionTitle("과목 균형")
                    AppCard {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            RadarChart(
                                axes = state.scores.map { it.subject.name },
                                values = state.scores.map { (it.average / 100.0).toFloat() },
                                color = MaterialTheme.colorScheme.primary,
                                secondary = state.progress.filter { p -> state.scores.any { it.subject.id == p.subject.id } }.map { it.myRatio },
                                secondaryColor = MaterialTheme.colorScheme.secondary,
                                size = 240,
                            )
                            Text("보라: 평균 점수 · 초록: 복습 완료율", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                SectionTitle("최근 2주 학습 시간")
                AppCard {
                    BarChart(
                        items = state.daily14.map { d -> BarItem(if (d.date.dayOfMonth % 2 == 1) d.date.dayOfMonth.toString() else "", d.minutes.toFloat(), MaterialTheme.colorScheme.primary) },
                        valueFormatter = { if (it >= 60) "${(it / 60).toInt()}h" else "${it.toInt()}m" },
                        height = 140,
                    )
                }
            }

            item {
                SectionTitle("이번 주 과목별 시간 배분")
                AppCard {
                    DonutChart(
                        slices = state.weeklyBySubject.map { w -> Slice(w.subject?.name ?: "기타", w.minutes.toFloat(), w.subject?.let { subjectColor(it.color) } ?: MaterialTheme.colorScheme.onSurfaceVariant) },
                        centerText = DateUtils.formatMinutes(state.weeklyBySubject.sumOf { it.minutes }),
                    )
                }
            }

            item {
                SectionTitle("시간대별 집중 분포")
                AppCard {
                    Column {
                        HourHeatStrip(state.byHour, MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text("누적 ${DateUtils.formatMinutes(state.totalMinutes)} · 진한 칸일수록 그 시간대에 많이 공부했어요", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item { SectionTitle(if (role == Role.PARENT) "메모 · 응원" else "학부모 메모", action = { TextButton(onClick = { showNote = true }) { Text("남기기") } }) }
            if (state.notes.isEmpty()) item { AppCard { EmptyState("아직 메모가 없어요") } }
            else items(state.notes, key = { "n" + it.id }) { n ->
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(n.text, style = MaterialTheme.typography.bodyLarge)
                            Text("${n.authorName} (${if (n.authorRole == "PARENT") "학부모" else "학생"}) · ${DateUtils.formatDate(DateUtils.toLocalDate(n.createdAt))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (n.authorRole == role.name) TextButton(onClick = { viewModel.deleteNote(n.id) }) { Text("삭제") }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showNote) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNote = false },
            title = { Text("메모 남기기") },
            text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("내용") }, modifier = Modifier.fillMaxWidth(), minLines = 2) },
            confirmButton = { TextButton(enabled = text.isNotBlank(), onClick = { viewModel.addNote(text); showNote = false }) { Text("저장") } },
            dismissButton = { TextButton(onClick = { showNote = false }) { Text("취소") } },
        )
    }
}

@Composable
fun InsightCard(insight: Insight, subjects: List<SubjectEntity>, onAction: ((InsightAction) -> Unit)?) {
    val (icon, color) = when (insight.kind) {
        InsightKind.STRENGTH -> Icons.Default.ThumbUp to MaterialTheme.colorScheme.secondary
        InsightKind.WEAKNESS -> Icons.Default.TrendingDown to MaterialTheme.colorScheme.error
        InsightKind.SUGGESTION -> Icons.Default.Lightbulb to MaterialTheme.colorScheme.primary
        InsightKind.ALERT -> Icons.Default.Warning to MaterialTheme.colorScheme.tertiary
    }
    val subject = subjects.firstOrNull { it.id == insight.subjectId }
    AppCard {
        Row(verticalAlignment = Alignment.Top) {
            Box(Modifier.background(color.copy(alpha = 0.12f), MaterialTheme.shapes.small).padding(8.dp)) {
                Icon(icon, contentDescription = insight.kind.label, tint = color)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(insight.kind.label, style = MaterialTheme.typography.labelSmall, color = color)
                    if (subject != null) SubjectTag(subject)
                }
                Text(insight.title, style = MaterialTheme.typography.titleSmall)
                Text(insight.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val action = insight.action
                if (action != null && onAction != null) {
                    TextButton(onClick = { onAction(action) }, contentPadding = PaddingValues(0.dp)) {
                        Text(
                            when (action) { is InsightAction.CreateTask -> "'${action.title}' 할 일로 추가" },
                        )
                    }
                }
            }
        }
    }
}
