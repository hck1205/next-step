package com.nextstep.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.layout.AppBarMenu
import com.nextstep.app.ui.components.layout.AppBarMenuItem
import com.nextstep.app.ui.components.layout.BackButton
import com.nextstep.app.ui.settings.components.ChildrenCard
import com.nextstep.app.ui.settings.components.GamifyCard
import com.nextstep.app.ui.settings.components.MembersCard
import com.nextstep.app.ui.settings.components.MentorModeCard
import com.nextstep.app.ui.settings.components.MyInfoCard
import com.nextstep.app.ui.settings.components.MySubjectsCard
import com.nextstep.app.ui.settings.components.PairingCodeCard
import com.nextstep.app.ui.settings.components.SettingsDialog
import com.nextstep.app.ui.settings.components.SettingsDialogs
import com.nextstep.app.ui.settings.components.SignOutSection
import com.nextstep.app.ui.settings.components.StudentYearCard

@Composable
fun SettingsScreen(caps: Capabilities, actions: SettingsActions, viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

/** 가족 탭: 자녀·내 정보·역할별 설정·구성원·연결 코드·계정(맨 아래). 다른 화면으로 가는 것(영상 저장소)은 머리 ⋮ 에. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsContent(state: SettingsUiState, caps: Capabilities, actions: SettingsActions, onEvent: (SettingsEvent) -> Unit) {
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }
    val open: (SettingsDialog) -> Unit = { dialog = it }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("가족") },
                navigationIcon = { BackButton(actions.onBack) },
                actions = { AppBarMenu(listOf(AppBarMenuItem("영상 저장소", Icons.Default.SmartDisplay, actions.onOpenContent))) },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FamilySection(state, caps, onEvent, open)
            RoleSection(state, caps, onEvent, open)
            SectionTitle("연결된 구성원")
            MembersCard(state.members, state.me, state.subjects, canRemove = caps.canRemoveMembers, onRemove = { open(SettingsDialog.RemoveMember(it)) })
            SectionTitle("연결 코드")
            PairingCodeCard(code = state.profile?.pairingCode, isStudent = caps.isStudent, syncStatus = state.syncStatus, syncAvailable = state.syncAvailable, onRequestSync = { onEvent(SettingsEvent.RequestSync) })
            SectionTitle("계정")
            SignOutSection(onSignOut = { open(SettingsDialog.SignOut) })
        }
    }
    SettingsDialogs(dialog, state, caps, onEvent, onDismiss = { dialog = null })
}

/** 자녀(맡은 학생) 목록과 내 정보. */
@Composable
private fun FamilySection(state: SettingsUiState, caps: Capabilities, onEvent: (SettingsEvent) -> Unit, open: (SettingsDialog) -> Unit) {
    if (caps.canLinkChildren) {
        SectionTitle(if (caps.isParent) "자녀" else "맡은 학생")
        ChildrenCard(
            children = state.children, activeFamilyId = state.activeFamilyId, error = state.childError,
            onSelect = { onEvent(SettingsEvent.SwitchChild(it)) },
            onAdd = if (caps.canAddChildren) ({ open(SettingsDialog.AddChild) }) else null,
            onLink = { open(SettingsDialog.LinkChild) },
        )
    }
    SectionTitle("내 정보")
    MyInfoCard(state.profile, state.me, showsRelation = caps.isParent, onRelation = { me, label -> onEvent(SettingsEvent.UpdateMyProfile(me.name, label)) })
}

/** 역할에 따라 보이는 설정: 학부모 겸 멘토 · 담당 과목 · 자녀 학년 · 레벨·배지. */
@Composable
private fun RoleSection(state: SettingsUiState, caps: Capabilities, onEvent: (SettingsEvent) -> Unit, open: (SettingsDialog) -> Unit) {
    if (caps.canToggleMentorMode) {
        SectionTitle("학부모 겸 멘토")
        MentorModeCard(enabled = state.me?.mentorEnabled == true, available = state.me != null, onChange = { onEvent(SettingsEvent.SetMentorEnabled(it)) })
    }
    if (caps.actsAsMentor) {
        SectionTitle("담당 과목", action = { TextButton(onClick = { open(SettingsDialog.Subjects) }) { Text("변경") } })
        MySubjectsCard(state.subjects, state.me?.subjectIdList ?: emptyList())
    }
    // 학년은 한 번 정하면 1년을 가므로 요약만 보이고, 바꾸려면 "고치기"로 창을 엽니다.
    SectionTitle("자녀 학년")
    StudentYearCard(
        yearLabel = state.yearLabel, birthDate = state.birthDate, ageLabel = state.ageLabel,
        level = state.chosenStudentLevel ?: state.autoStudentLevel, chosen = state.chosenStudentLevel != null,
        onEdit = { open(SettingsDialog.EditYear) },
    )
    if (caps.canToggleGamification(state.gameStyle)) {
        SectionTitle("레벨·배지")
        GamifyCard(
            style = state.gameStyle, forStudent = caps.isStudent, enabled = state.student?.gamify ?: true, available = state.student != null,
            onChange = { onEvent(SettingsEvent.SetGamify(it)) },
        )
    }
}
