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
import com.nextstep.app.ui.mentor.components.MentorCardBody
import com.nextstep.app.ui.mentor.components.mentorCardTitle
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.domain.today.MentorTodayCard
import com.nextstep.app.ui.components.layout.DetailSheet
import com.nextstep.app.ui.components.layout.todayBoard
import androidx.compose.runtime.saveable.rememberSaveable

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
    var filter by rememberSaveable { mutableStateOf<Concern?>(null) }
    var sheet by remember { mutableStateOf<MentorTodayCard?>(null) }
    sheet?.let { card ->
        DetailSheet(mentorCardTitle(card).ifBlank { card.title }, onDismiss = { sheet = null }) {
            MentorCardBody(card, state, actions, onEvent, compact = false, onChangeSubjects = { sheet = null; showSubjects = true }, onAssign = { sheet = null; showAssign = true })
        }
    }

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
            // 관심사 칩 → "전체"는 관심사마다 카드 슬라이드, 칩을 고르면 그 관심사만 크게. 펼치기는 자세히 시트로.
            todayBoard(
                groups = state.todayGroups, filter = filter, onFilter = { filter = it },
                title = { mentorCardTitle(it) }, key = { it.name }, onExpand = { sheet = it },
                body = { card, compact -> MentorCardBody(card, state, actions, onEvent, compact, onChangeSubjects = { showSubjects = true }, onAssign = { showAssign = true }) },
            )
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
