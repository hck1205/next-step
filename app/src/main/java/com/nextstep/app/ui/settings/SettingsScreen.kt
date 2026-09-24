package com.nextstep.app.ui.settings

import com.nextstep.app.ui.settings.components.StudentScreenCard
import com.nextstep.app.ui.settings.components.RelationPicker
import com.nextstep.app.ui.settings.components.ChildrenCard
import com.nextstep.app.ui.settings.components.AddChildDialog
import com.nextstep.app.ui.components.dialog.TextInputDialog
import com.nextstep.app.data.model.GuardianRelation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.LinkCard
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.card.SubjectTag
import com.nextstep.app.ui.components.dialog.ConfirmDialog
import com.nextstep.app.ui.components.dialog.SubjectSelectDialog
import com.nextstep.app.ui.settings.components.InfoRow
import com.nextstep.app.ui.settings.components.MembersCard
import com.nextstep.app.ui.settings.components.MentorModeCard
import com.nextstep.app.ui.settings.components.PairingCodeCard
import com.nextstep.app.ui.settings.components.StudentProfileCard

@Composable
fun SettingsScreen(caps: Capabilities, actions: SettingsActions, viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsContent(state: SettingsUiState, caps: Capabilities, actions: SettingsActions, onEvent: (SettingsEvent) -> Unit) {
    var confirmSignOut by remember { mutableStateOf(false) }
    var confirmRemove by remember { mutableStateOf<String?>(null) }
    var showSubjects by remember { mutableStateOf(false) }
    var showAddChild by remember { mutableStateOf(false) }
    var showLinkChild by remember { mutableStateOf(false) }
    val profile = state.profile

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("가족") },
                navigationIcon = { actions.onBack?.let { back -> IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LinkCard("콘텐츠 저장소", "좋은 유튜브 강의를 등록해 두면 아이 진도에 맞춰 추천돼요", onClick = actions.onOpenContent)

            if (caps.canLinkChildren) {
                SectionTitle(if (caps.isParent) "자녀" else "맡은 학생")
                ChildrenCard(
                    children = state.children, activeFamilyId = state.activeFamilyId, error = state.childError,
                    onSelect = { onEvent(SettingsEvent.SwitchChild(it)) },
                    onAdd = if (caps.canAddChildren) ({ showAddChild = true }) else null,
                    onLink = { showLinkChild = true },
                )
            }

            SectionTitle("내 정보")
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    InfoRow("역할", state.me?.roleLabel ?: profile?.role?.label ?: "-")
                    InfoRow("이름", profile?.displayName ?: "-")
                    InfoRow("학생", profile?.studentName ?: "-")
                    if (caps.isParent) state.me?.let { me ->
                        RelationPicker(GuardianRelation.fromLabel(me.title), onSelect = { onEvent(SettingsEvent.UpdateMyProfile(me.name, it?.label.orEmpty())) })
                    }
                }
            }

            if (caps.canToggleMentorMode) {
                SectionTitle("학부모 겸 멘토")
                MentorModeCard(enabled = state.me?.mentorEnabled == true, available = state.me != null, onChange = { onEvent(SettingsEvent.SetMentorEnabled(it)) })
            }
            if (caps.actsAsMentor) {
                SectionTitle("담당 과목", action = { TextButton(onClick = { showSubjects = true }) { Text("변경") } })
                AppCard {
                    val mine = state.me?.subjectIdList ?: emptyList()
                    if (mine.isEmpty()) Text("전 과목 담당", style = MaterialTheme.typography.bodyMedium)
                    else Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { state.subjects.filter { it.id in mine }.forEach { SubjectTag(it) } }
                }
            }

            SectionTitle("자녀 생년월일 · 학년 · 성장 단계")
            StudentProfileCard(
                birthDate = state.birthDate, ageLabel = state.ageLabel, gradeYear = state.student?.gradeYear ?: 0,
                onBirthDate = { onEvent(SettingsEvent.SetBirthDate(it)) }, onGradeYear = { onEvent(SettingsEvent.SetGradeYear(it)) },
            )
            if (caps.canChooseStudentScreen) state.autoStudentLevel?.let { auto ->
                SectionTitle("아이 화면")
                StudentScreenCard(auto = auto, chosen = state.chosenStudentLevel, onChoose = { onEvent(SettingsEvent.SetStudentLevel(it)) })
            }

            SectionTitle("연결된 구성원")
            MembersCard(state.members, state.me, state.subjects, canRemove = caps.canRemoveMembers, onRemove = { confirmRemove = it })

            SectionTitle("연결 코드")
            PairingCodeCard(code = profile?.pairingCode, isStudent = caps.isStudent, syncStatus = state.syncStatus, syncAvailable = state.syncAvailable, onRequestSync = { onEvent(SettingsEvent.RequestSync) })

            SectionTitle("계정")
            Button(onClick = { confirmSignOut = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("이 기기에서 연결 해제") }
            Text(
                "연결 해제하면 이 기기의 역할·가족 정보가 초기화되고 온보딩 화면으로 돌아갑니다. 서버에 동기화된 데이터는 유지됩니다.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (confirmSignOut) {
        ConfirmDialog("연결 해제", "정말 이 기기에서 연결을 해제할까요?", confirmLabel = "해제", onConfirm = { onEvent(SettingsEvent.SignOut) }, onDismiss = { confirmSignOut = false })
    }
    confirmRemove?.let { id ->
        val name = state.members.firstOrNull { it.id == id }?.name ?: ""
        ConfirmDialog("연결 끊기", "'$name' 님을 구성원 목록에서 제거할까요? 상대 기기에서는 다시 코드를 입력해야 연결됩니다.", confirmLabel = "제거", onConfirm = { onEvent(SettingsEvent.RemoveMember(id)) }, onDismiss = { confirmRemove = null })
    }
    if (showAddChild) AddChildDialog(onConfirm = { name, birth -> onEvent(SettingsEvent.AddChild(name, birth)) }, onDismiss = { showAddChild = false })
    if (showLinkChild) TextInputDialog(title = "코드로 연결", label = "연결 코드 6자리", confirmLabel = "연결", onConfirm = { onEvent(SettingsEvent.LinkChild(it)) }, onDismiss = { showLinkChild = false })
    if (showSubjects) {
        SubjectSelectDialog(state.subjects, state.me?.subjectIdList ?: emptyList(), onDismiss = { showSubjects = false }) { onEvent(SettingsEvent.SetMySubjects(it)) }
    }
}
