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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.layout.AppBarMenu
import com.nextstep.app.ui.components.layout.AppBarMenuItem
import com.nextstep.app.ui.components.layout.BackButton
import com.nextstep.app.ui.components.layout.CompactTopBar
import com.nextstep.app.ui.settings.components.ChildrenCard
import com.nextstep.app.ui.settings.components.MembersCard
import com.nextstep.app.ui.settings.components.SettingRow
import com.nextstep.app.ui.settings.components.SettingsDialog
import com.nextstep.app.ui.settings.components.SettingsDialogs

@Composable
fun SettingsScreen(caps: Capabilities, actions: SettingsActions, viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

/** 가족 탭은 세 덩어리: 자녀(학부모·멘토) · 우리 가족(구성원) · 설정(한 줄씩, 누르면 창). 맨 아래 연결 해제. 영상 저장소는 머리 ⋮ 에. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsContent(state: SettingsUiState, caps: Capabilities, actions: SettingsActions, onEvent: (SettingsEvent) -> Unit) {
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }
    val open: (SettingsDialog) -> Unit = { dialog = it }
    Scaffold(
        topBar = {
            CompactTopBar(
                title = "가족",
                navigationIcon = { BackButton(actions.onBack) },
                actions = { AppBarMenu(listOf(AppBarMenuItem("영상 저장소", Icons.Default.SmartDisplay, actions.onOpenContent))) },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ChildrenSection(state, caps, onEvent, open)
            SectionTitle("우리 가족")
            MembersCard(state.members.filter { caps.canSeeGuardians || !it.isParent }, state.me, state.subjects, canRemove = caps.canRemoveMembers, onRemove = { open(SettingsDialog.RemoveMember(it)) })
            SectionTitle("설정")
            AppCard(padded = false) { Column { SettingsRows(state, caps, onEvent, open) } }
            TextButton(onClick = { open(SettingsDialog.SignOut) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("이 기기에서 연결 해제", color = MaterialTheme.colorScheme.error)
            }
        }
    }
    SettingsDialogs(dialog, state, caps, onEvent, onDismiss = { dialog = null })
}

/** 자녀(맡은 학생) 고르기·추가·연결. 학생에게는 없습니다. */
@Composable
private fun ChildrenSection(state: SettingsUiState, caps: Capabilities, onEvent: (SettingsEvent) -> Unit, open: (SettingsDialog) -> Unit) {
    if (!caps.canLinkChildren) return
    SectionTitle(if (caps.isParent) "자녀" else "맡은 학생")
    ChildrenCard(
        children = state.children, activeFamilyId = state.activeFamilyId, error = state.childError,
        onSelect = { onEvent(SettingsEvent.SwitchChild(it)) },
        onAdd = if (caps.canAddChildren) ({ open(SettingsDialog.AddChild) }) else null,
        onLink = { open(SettingsDialog.LinkChild) },
    )
}

/**
 * 설정은 한 줄씩: 이름 + 지금 값. 자세한 것은 누르면 여는 창에서(내 정보 · 자녀 학년 · 담당 과목 · 연결 코드),
 * 켜고 끄는 것(멘토 겸하기 · 레벨·배지)은 그 줄의 스위치로. 역할마다 보이는 줄은 caps 가 정합니다.
 */
@Composable
private fun SettingsRows(state: SettingsUiState, caps: Capabilities, onEvent: (SettingsEvent) -> Unit, open: (SettingsDialog) -> Unit) {
    val me = listOfNotNull(state.me?.roleLabel ?: state.profile?.role?.label, state.profile?.displayName).joinToString(" · ")
    SettingRow("내 정보", me.ifBlank { "-" }, onClick = { open(SettingsDialog.MyInfo) })
    val level = state.chosenStudentLevel ?: state.autoStudentLevel
    val year = listOfNotNull(state.yearLabel ?: "학년 미정", state.ageLabel, level?.let { "아이 화면 ${it.label}" }).joinToString(" · ")
    SettingRow("자녀 학년", year, onClick = if (caps.canEditStudentYear) ({ open(SettingsDialog.EditYear) }) else null)
    if (caps.actsAsMentor) {
        val mine = state.me?.subjectIdList.orEmpty()
        val names = state.subjects.filter { it.id in mine }.joinToString(", ") { it.name }.ifBlank { "전 과목" }
        SettingRow("담당 과목", names, onClick = { open(SettingsDialog.Subjects) })
    }
    if (caps.canToggleMentorMode) {
        SettingRow("멘토 역할 겸하기", "직접 가르친다면 켜요. 로드맵·과제·진도 관리가 열려요.") {
            Switch(checked = state.me?.mentorEnabled == true, onCheckedChange = { onEvent(SettingsEvent.SetMentorEnabled(it)) }, enabled = state.me != null)
        }
    }
    if (caps.canToggleGamification(state.gameStyle)) {
        val style = state.gameStyle
        SettingRow(if (caps.isStudent) "${style.title} 보기" else "${style.title.removePrefix("나의 ")} 보여 주기", style.summary) {
            Switch(checked = state.student?.gamify ?: true, onCheckedChange = { onEvent(SettingsEvent.SetGamify(it)) }, enabled = state.student != null)
        }
    }
    SettingRow("알림", if (caps.isFamily) "아침에 오늘 챙길 것, 일요일 저녁에 주말 이야기를 한 번씩" else "아침에 오늘 챙길 것을 한 번") {
        Switch(checked = state.noticesOn, onCheckedChange = { onEvent(SettingsEvent.SetNotices(it)) })
    }
    SettingRow("연결 코드", state.profile?.pairingCode ?: "------", onClick = { open(SettingsDialog.PairingCode) })
}

