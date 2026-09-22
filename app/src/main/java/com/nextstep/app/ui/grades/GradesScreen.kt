package com.nextstep.app.ui.grades

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.local.GradeEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.model.ExamType
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.DateField
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.LineChart
import com.nextstep.app.ui.components.LineSeries
import com.nextstep.app.ui.components.OptionPicker
import com.nextstep.app.ui.components.RadarChart
import com.nextstep.app.ui.components.SectionTitle
import com.nextstep.app.ui.components.StatTile
import com.nextstep.app.ui.components.SubjectPicker
import com.nextstep.app.ui.components.SubjectTag
import com.nextstep.app.ui.components.subjectColor
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen(role: Role, viewModel: GradesViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showEdit by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<GradeEntity?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (role == Role.PARENT) "자녀 성적" else "내 성적") }) },
        floatingActionButton = {
            if (state.subjects.isNotEmpty()) FloatingActionButton(onClick = { editing = null; showEdit = true }) { Icon(Icons.Default.Add, contentDescription = "성적 추가") }
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
                    StatTile("전체 평균", state.overallAverage?.let { String.format(Locale.ROOT, "%.1f", it) } ?: "-", Modifier.weight(1f))
                    StatTile("최고 과목", best?.subject?.name ?: "-", Modifier.weight(1f), tint = MaterialTheme.colorScheme.secondary, sub = best?.let { String.format(Locale.ROOT, "%.1f점", it.average) })
                    StatTile("보완 과목", weak?.subject?.name ?: "-", Modifier.weight(1f), tint = MaterialTheme.colorScheme.tertiary, sub = weak?.let { String.format(Locale.ROOT, "%.1f점", it.average) })
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
                    FilterChip(selected = state.filterSubjectId == null, onClick = { viewModel.setFilter(null) }, label = { Text("전체") })
                    state.subjects.forEach { s ->
                        FilterChip(selected = state.filterSubjectId == s.id, onClick = { viewModel.setFilter(s.id) }, label = { Text(s.name) })
                    }
                }
            }
            if (state.filtered.isEmpty()) item { AppCard { EmptyState(if (state.subjects.isEmpty()) "진도 탭에서 과목을 먼저 추가하세요" else "성적을 추가하면 추이와 강점 분석이 표시돼요") } }
            items(state.filtered, key = { it.id }) { g ->
                GradeRow(g, state.subjects, onClick = { editing = g; showEdit = true })
            }
        }
    }

    if (showEdit) {
        GradeEditDialog(editing, state.subjects, onDismiss = { showEdit = false }, onDelete = editing?.let { g -> { viewModel.delete(g.id) } }) { subjectId, title, type, score, max, classAvg, date, memo ->
            viewModel.save(editing, subjectId, title, type, score, max, classAvg, date, memo)
        }
    }
}

/** 시험 날짜순으로 정렬해 과목별 계열을 그립니다. x축은 날짜별 고유 인덱스. */
@Composable
private fun GradeTrendChart(state: GradesUiState) {
    val grades = state.filtered.sortedBy { it.date }
    val dates = grades.map { it.date }.distinct()
    val subjects = if (state.filterSubjectId == null) state.subjects.filter { s -> grades.any { it.subjectId == s.id } } else state.subjects.filter { it.id == state.filterSubjectId }
    val series = subjects.map { s ->
        LineSeries(
            name = s.name, color = subjectColor(s.color),
            points = dates.map { d -> grades.lastOrNull { it.subjectId == s.id && it.date == d }?.percent?.toFloat() },
        )
    }
    val classSeries = if (state.filterSubjectId != null) {
        val pts = dates.map { d -> grades.lastOrNull { it.date == d }?.classAverage?.toFloat() }
        if (pts.any { it != null }) LineSeries("반 평균", MaterialTheme.colorScheme.onSurfaceVariant, pts) else null
    } else null
    LineChart(
        series = series + listOfNotNull(classSeries),
        xLabels = dates.map { DateUtils.formatShortDate(DateUtils.fromEpochDay(it)) },
    )
}

@Composable
private fun GradeRow(g: GradeEntity, subjects: List<SubjectEntity>, onClick: () -> Unit) {
    val subject = subjects.firstOrNull { it.id == g.subjectId }
    AppCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(g.title, style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SubjectTag(subject)
                    Text(g.examType.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(DateUtils.formatDate(DateUtils.fromEpochDay(g.date)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${g.score.trim()} / ${g.maxScore.trim()}",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    color = when {
                        g.percent >= 90 -> MaterialTheme.colorScheme.secondary
                        g.percent < 70 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                )
                g.classAverage?.let { avg ->
                    val diff = g.score - avg
                    Text(
                        "반 평균 ${avg.trim()} (${if (diff >= 0) "+" else ""}${diff.trim()})",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (diff >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

private fun Double.trim(): String = if (this == Math.floor(this)) toInt().toString() else String.format(Locale.ROOT, "%.1f", this)

@Composable
fun GradeEditDialog(
    existing: GradeEntity?,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: (String, String, ExamType, Double, Double, Double?, LocalDate, String) -> Unit,
) {
    var subjectId by remember { mutableStateOf(existing?.subjectId ?: subjects.firstOrNull()?.id ?: "") }
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var type by remember { mutableStateOf(existing?.examType ?: ExamType.QUIZ) }
    var score by remember { mutableStateOf(existing?.score?.trim() ?: "") }
    var max by remember { mutableStateOf(existing?.maxScore?.trim() ?: "100") }
    var classAvg by remember { mutableStateOf(existing?.classAverage?.trim() ?: "") }
    var date by remember { mutableStateOf(existing?.let { DateUtils.fromEpochDay(it.date) } ?: LocalDate.now()) }
    var memo by remember { mutableStateOf(existing?.memo ?: "") }
    val valid = subjectId.isNotBlank() && title.isNotBlank() && score.toDoubleOrNull() != null && (max.toDoubleOrNull() ?: 0.0) > 0
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "성적 추가" else "성적 편집") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SubjectPicker(subjects, subjectId, onSelect = { it?.let { id -> subjectId = id } }, allowNone = false)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("시험명 (예: 1학기 중간고사)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OptionPicker(ExamType.entries, type, label = { it.label }, onSelect = { type = it })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = score, onValueChange = { score = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("점수") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = max, onValueChange = { max = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("만점") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = classAvg, onValueChange = { classAvg = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("반 평균 (선택)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                DateField("시험일", date, onChange = { date = it })
                OutlinedTextField(value = memo, onValueChange = { memo = it }, label = { Text("메모 (틀린 유형 등)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = {
                onSave(subjectId, title.trim(), type, score.toDouble(), max.toDouble(), classAvg.toDoubleOrNull(), date, memo.trim()); onDismiss()
            }) { Text("저장") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = { onDelete(); onDismiss() }) { Text("삭제", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("취소") }
            }
        },
    )
}
