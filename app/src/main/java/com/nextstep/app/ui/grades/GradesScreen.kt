package com.nextstep.app.ui.grades

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.dialog.GradeEditDialog
import com.nextstep.app.ui.components.chart.RadarChart
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.card.StatTile
import com.nextstep.app.ui.grades.components.GradeRow
import com.nextstep.app.ui.grades.components.GradeTrendChart
import com.nextstep.app.ui.common.oneDecimal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun GradesScreen(caps: Capabilities, viewModel: GradesViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    GradesContent(state = state, caps = caps, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GradesContent(state: GradesUiState, caps: Capabilities, onEvent: (GradesEvent) -> Unit) {
    var showEdit by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<GradeEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            if (state.subjects.isNotEmpty() && caps.canEditGrades) FloatingActionButton(onClick = { editing = null; showEdit = true }) { Icon(Icons.Default.Add, contentDescription = "성적 추가") }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                val best = state.scores.maxByOrNull { it.average }
                val weak = state.scores.minByOrNull { it.average }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("전체 평균", state.overallAverage?.oneDecimal() ?: "-", Modifier.weight(1f))
                    StatTile("최고 과목", best?.subject?.name ?: "-", Modifier.weight(1f), tint = MaterialTheme.colorScheme.secondary, sub = best?.let { "${it.average.oneDecimal()}점" })
                    StatTile("보완 과목", weak?.subject?.name ?: "-", Modifier.weight(1f), tint = MaterialTheme.colorScheme.tertiary, sub = weak?.let { "${it.average.oneDecimal()}점" })
                }
            }

            if (state.scores.size >= 3) {
                item {
                    SectionTitle("과목별 균형 (평균 점수)")
                    AppCard {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            RadarChart(
                                axes = state.scores.map { it.subject.name },
                                values = state.scores.map { (it.average / 100.0).toFloat() },
                                color = MaterialTheme.colorScheme.primary,
                                chartSize = 240,
                            )
                        }
                    }
                }
            }

            if (state.grades.isNotEmpty()) {
                item {
                    SectionTitle("성적 추이")
                    AppCard { GradeTrendChart(state) }
                }
            }

            item {
                SectionTitle("성적 목록")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = state.filterSubjectId == null, onClick = { onEvent(GradesEvent.SetFilter(null)) }, label = { Text("전체") })
                    state.subjects.forEach { s ->
                        FilterChip(selected = state.filterSubjectId == s.id, onClick = { onEvent(GradesEvent.SetFilter(s.id)) }, label = { Text(s.name) })
                    }
                }
            }
            if (state.filtered.isEmpty()) item { AppCard { EmptyState(if (state.subjects.isEmpty()) "진도 탭에서 과목을 먼저 추가하세요" else "성적을 추가하면 추이와 강점 분석이 표시돼요") } }
            items(state.filtered, key = { it.id }) { g ->
                GradeRow(g, state.subjects, onClick = { if (caps.canEditGrades) { editing = g; showEdit = true } })
            }
        }
    }

    if (showEdit) {
        GradeEditDialog(editing, state.subjects, onDismiss = { showEdit = false }, onDelete = editing?.let { g -> { onEvent(GradesEvent.Delete(g.id)) } }) { subjectId, title, type, score, max, classAvg, date, memo ->
            onEvent(GradesEvent.Save(editing, subjectId, title, type, score, max, classAvg, date, memo))
        }
    }
}
