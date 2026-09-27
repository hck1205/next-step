package com.nextstep.app.ui.mentor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.domain.today.MentorTodayCard
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.SyncStatusBadge
import com.nextstep.app.ui.components.dialog.AssignTaskDialog
import com.nextstep.app.ui.components.dialog.SubjectSelectDialog
import com.nextstep.app.ui.components.input.ChildPicker
import com.nextstep.app.ui.components.layout.AppBarMenu
import com.nextstep.app.ui.components.layout.AppBarMenuItem
import com.nextstep.app.ui.components.layout.BackButton
import com.nextstep.app.ui.components.layout.DetailSheet
import com.nextstep.app.ui.components.layout.ScreenPadding
import com.nextstep.app.ui.components.layout.todayBoard
import com.nextstep.app.ui.mentor.components.MentorCardBody
import com.nextstep.app.ui.mentor.components.mentorCardTitle

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

    Scaffold(topBar = { MentorTopBar(state, actions) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = ScreenPadding.list,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
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

/** 머리: 학생 · 내 구분, 오른쪽에 학생 고르기와 ⋮(영상 저장소). 학부모가 열었으면 뒤로 가기. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MentorTopBar(state: MentorDashboardUiState, actions: MentorDashboardActions) {
    TopAppBar(
        title = {
            Column {
                Text("${state.studentName.ifBlank { "학생" }} · ${state.me?.title?.ifBlank { null } ?: Role.MENTOR.label}", style = MaterialTheme.typography.titleLarge)
                SyncStatusBadge(state.syncStatus)
            }
        },
        navigationIcon = { BackButton(actions.onBack) },
        actions = {
            ChildPicker(state.students, state.activeFamilyId, onSelect = actions.onSwitchChild, onAdd = null)
            AppBarMenu(listOf(AppBarMenuItem(MentorTodayCard.CONTENT.title, Icons.Default.SmartDisplay, actions.onOpenContent)))
        },
    )
}
