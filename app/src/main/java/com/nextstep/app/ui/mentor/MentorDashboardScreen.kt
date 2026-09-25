package com.nextstep.app.ui.mentor

import com.nextstep.app.ui.components.input.ChildSwitcher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.card.InsightCard
import com.nextstep.app.ui.components.card.LinkCard
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.card.StageCard
import com.nextstep.app.ui.components.card.SyncStatusBadge
import com.nextstep.app.ui.components.card.subjectColor
import com.nextstep.app.ui.components.chart.BarChart
import com.nextstep.app.ui.components.chart.BarItem
import com.nextstep.app.ui.components.dialog.AssignTaskDialog
import com.nextstep.app.ui.components.dialog.SubjectSelectDialog
import com.nextstep.app.ui.mentor.components.MentorGradeRow
import com.nextstep.app.ui.mentor.components.MentorProgressCard
import com.nextstep.app.ui.mentor.components.MentorStatsRow
import com.nextstep.app.ui.mentor.components.MentorSubjectsCard
import com.nextstep.app.ui.mentor.components.MentorTaskRow

@Composable
fun MentorDashboardScreen(actions: MentorDashboardActions, viewModel: MentorDashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MentorDashboardContent(state = state, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MentorDashboardContent(state: MentorDashboardUiState, actions: MentorDashboardActions, onEvent: (MentorDashboardEvent) -> Unit) {
    var showSubjects by remember { mutableStateOf(false) }
    var showAssign by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("${state.studentName.ifBlank { "학생" }} · ${state.me?.title?.ifBlank { null } ?: Role.MENTOR.label}", style = MaterialTheme.typography.titleLarge)
                        SyncStatusBadge(state.syncStatus)
                    }
                },
                navigationIcon = { if (actions.onBack != null) IconButton(onClick = actions.onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } },
                actions = { IconButton(onClick = actions.onOpenSettings) { Icon(Icons.Default.Settings, contentDescription = "설정") } },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.students.size > 1) item { ChildSwitcher(state.students, state.activeFamilyId, onSelect = actions.onSwitchChild, onAdd = actions.onOpenSettings) }
            item { StageCard(stage = state.stage, gradeLabel = null, headline = state.stage?.let { "이 시기의 큐레이팅 기준" }, body = state.mentorTip, experience = null, onSetGrade = actions.onOpenJourney) }
            item {
                val r = state.roadmap
                LinkCard(
                    title = "학습 로드맵 큐레이팅",
                    description = if (r.isEmpty) "무엇을 어떤 순서로, 어떤 자료로, 언제까지 공부할지 제안해 보세요"
                    else "진행 중 ${r.inProgress} · 완료 ${r.done}/${r.total}" + (if (r.overdue > 0) " · 기한 지남 ${r.overdue}" else ""),
                    onClick = actions.onOpenRoadmap,
                )
            }
            item { LinkCard("콘텐츠 저장소", "좋은 유튜브 강의를 링크로 등록하면 자동 분류되고 학생 진도에 맞춰 추천돼요", onClick = actions.onOpenContent) }
            item { MentorSubjectsCard(state.subjects, state.needsSubjectSetup, onChange = { showSubjects = true }) }

            if (state.otherMentors.isNotEmpty()) {
                item {
                    AppCard {
                        Text(
                            "함께 연결된 멘토: " + state.otherMentors.joinToString { m -> m.name + (if (m.title.isNotBlank()) " (${m.title})" else "") },
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item { MentorStatsRow(state.weekMinutes, state.myTasks.size, state.averageScore) }

            if (state.weeklyBySubject.isNotEmpty()) {
                item {
                    SectionTitle("이번 주 과목별 학습 시간 (선: 목표)")
                    AppCard {
                        BarChart(
                            items = state.weeklyBySubject.map { w ->
                                BarItem(w.subject!!.name, w.minutes.toFloat(), subjectColor(w.subject.color), goal = w.goalMinutes.toFloat().takeIf { it > 0 })
                            },
                            valueFormatter = { DateUtils.formatMinutes(it.toInt()) },
                        )
                    }
                }
            }

            if (state.progress.isNotEmpty()) {
                item { SectionTitle("진도 · 학급 진도 대비 복습률 (눌러서 단원 관리)") }
                items(state.progress, key = { it.subject.id }) { p -> MentorProgressCard(p, onOpen = { actions.onOpenSubject(p.subject.id) }) }
            }

            item { SectionTitle("분석") }
            items(state.insights) { InsightCard(it, state.allSubjects, onAction = null) }

            if (state.recentGrades.isNotEmpty()) {
                item { SectionTitle("최근 성적") }
                items(state.recentGrades, key = { "g" + it.id }) { g -> MentorGradeRow(g, state.allSubjects.firstOrNull { it.id == g.subjectId }) }
            }

            item { SectionTitle("내가 낸 과제", action = { TextButton(onClick = { showAssign = true }) { Text("과제 내기") } }) }
            if (state.myTasks.isEmpty()) item { AppCard { EmptyState("미완료 과제가 없어요") } }
            else items(state.myTasks, key = { "t" + it.id }) { t ->
                MentorTaskRow(t, state.allSubjects.firstOrNull { it.id == t.subjectId }, onCancel = { onEvent(MentorDashboardEvent.DeleteTask(t.id)) })
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showSubjects) {
        SubjectSelectDialog(state.allSubjects, state.me?.subjectIdList ?: emptyList(), onDismiss = { showSubjects = false }) { onEvent(MentorDashboardEvent.SetSubjects(it)) }
    }
    if (showAssign) {
        AssignTaskDialog(
            subjects = state.subjects, title = "과제 내기", label = "과제 내용",
            defaultSubjectId = state.subjects.firstOrNull()?.id, defaultDue = DateUtils.today().plusDays(1),
            onDismiss = { showAssign = false },
        ) { title, subjectId, type, due -> onEvent(MentorDashboardEvent.AssignTask(title, subjectId, type, due)) }
    }
}
