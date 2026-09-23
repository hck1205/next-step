package com.nextstep.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.model.Role
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.dialog.ConfirmDialog
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.dialog.SubjectSelectDialog
import com.nextstep.app.ui.components.card.SubjectTag
import com.nextstep.app.ui.components.card.SyncStatusBadge
import com.nextstep.app.ui.settings.components.InfoRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.nextstep.app.ui.components.input.GradePicker
import com.nextstep.app.ui.components.input.DateField

@Composable
fun SettingsScreen(actions: SettingsActions, viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsContent(state = state, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsContent(state: SettingsUiState, actions: SettingsActions, onEvent: (SettingsEvent) -> Unit) {
    val clipboard = LocalClipboardManager.current
    var confirmSignOut by remember { mutableStateOf(false) }
    var confirmRemove by remember { mutableStateOf<String?>(null) }
    var showSubjects by remember { mutableStateOf(false) }
    val profile = state.profile
    val role = profile?.role

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("가족") },
                navigationIcon = { actions.onBack?.let { back -> IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AppCard(onClick = actions.onOpenContent) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("콘텐츠 저장소", style = MaterialTheme.typography.titleMedium)
                        Text("좋은 유튜브 강의를 등록해 두면 아이 진도에 맞춰 추천돼요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = actions.onOpenContent) { Text("열기") }
                }
            }
            SectionTitle("내 정보")
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    InfoRow("역할", profile?.role?.label ?: "-")
                    InfoRow("이름", profile?.displayName ?: "-")
                    InfoRow("학생", profile?.studentName ?: "-")
                }
            }

            if (role == Role.PARENT) {
                SectionTitle("학부모 겸 멘토")
                AppCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("멘토 역할 겸하기", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("직접 자녀를 가르친다면 켜세요. 로드맵 큐레이팅, 과제 배정, 단원·학급 진도 관리가 열립니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = state.me?.mentorEnabled == true, onCheckedChange = { onEvent(SettingsEvent.SetMentorEnabled(it)) }, enabled = state.me != null)
                        }
                    }
                }
            }
            if (role == Role.MENTOR || state.me?.mentorEnabled == true) {
                SectionTitle("담당 과목", action = { TextButton(onClick = { showSubjects = true }) { Text("변경") } })
                AppCard {
                    val mine = state.me?.subjectIdList ?: emptyList()
                    if (mine.isEmpty()) Text("전 과목 담당", style = MaterialTheme.typography.bodyMedium)
                    else Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { state.subjects.filter { it.id in mine }.forEach { SubjectTag(it) } }
                }
            }

            SectionTitle("자녀 생년월일 · 학년 · 성장 단계")
            AppCard {
                val student = state.members.firstOrNull { it.isStudent }
                val birth = student?.birthDate?.let { java.time.LocalDate.ofEpochDay(it) }
                Column {
                    DateField(label = "생년월일", date = birth ?: java.time.LocalDate.now().minusYears(3), onChange = { onEvent(SettingsEvent.SetBirthDate(it)) })
                    if (birth == null) Text("생년월일을 넣으면 여정 타임라인(어린이집 대기, 검진, 입학, 입시 일정)이 자동으로 채워져요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else Text("${com.nextstep.app.domain.growth.GrowthStage.ageLabel(birth, java.time.LocalDate.now())} · 학년은 생년월일로 자동 계산되며 아래에서 직접 바꿀 수 있어요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    GradePicker(gradeYear = student?.gradeYear ?: 0, onSelect = { onEvent(SettingsEvent.SetGradeYear(it)) })
                    Text("학년은 추천 영상의 학년대, 학습 계획 길이, 학부모·멘토 가이드를 정합니다. 학생이나 학부모가 바꿀 수 있어요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            SectionTitle("연결된 구성원")
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.members.isEmpty()) Text("구성원 정보가 아직 동기화되지 않았어요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    state.members.forEach { m ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(m.name + (if (m.id == state.me?.id) " (나)" else ""), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                val detail = buildList {
                                    add(Role.labelOf(m.role))
                                    if (m.title.isNotBlank()) add(m.title)
                                    if (m.isParent && m.mentorEnabled) add("멘토 겸")
                                    if (m.isMentor || m.mentorEnabled) add(if (m.subjectIdList.isEmpty()) "전 과목" else state.subjects.filter { it.id in m.subjectIdList }.joinToString { it.name })
                                }
                                Text(detail.joinToString(" · "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            val canRemove = m.id != state.me?.id && !m.isStudent && (role == Role.STUDENT || role == Role.PARENT)
                            if (canRemove) TextButton(onClick = { confirmRemove = m.id }) { Text("연결 끊기") }
                        }
                    }
                    Text(
                        "학부모(엄마, 아빠 등)와 멘토(선생님·과외·튜터)는 여러 명이 같은 코드로 연결할 수 있어요.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            SectionTitle("연결 코드")
            AppCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (role == Role.STUDENT) "학부모·멘토 앱에서 아래 코드를 입력하면 연결돼요" else "연결된 학생 코드 · 다른 학부모나 멘토에게 공유할 수 있어요",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(profile?.pairingCode ?: "------", fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = 6.sp, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { profile?.pairingCode?.let { clipboard.setText(AnnotatedString(it)) } }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "복사")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    SyncStatusBadge(state.syncStatus)
                    if (!state.syncAvailable) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Firebase 가 설정되지 않아 이 기기에만 저장됩니다. app/google-services.json 을 추가하고 다시 빌드하면 학생·학부모 기기 간 실시간 동기화가 켜집니다.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { onEvent(SettingsEvent.RequestSync) }) { Text("지금 동기화") }
                    }
                }
            }

            SectionTitle("계정")
            Button(
                onClick = { confirmSignOut = true },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) { Text("이 기기에서 연결 해제") }
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
    if (showSubjects) {
        SubjectSelectDialog(state.subjects, state.me?.subjectIdList ?: emptyList(), onDismiss = { showSubjects = false }) { onEvent(SettingsEvent.SetMySubjects(it)) }
    }
}
