package com.nextstep.app.ui.settings.components

import androidx.compose.runtime.Composable
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.components.dialog.ConfirmDialog
import com.nextstep.app.ui.components.dialog.SubjectSelectDialog
import com.nextstep.app.ui.components.dialog.TextInputDialog
import com.nextstep.app.ui.components.layout.DetailSheet
import com.nextstep.app.ui.settings.SettingsEvent
import com.nextstep.app.ui.settings.SettingsUiState

/** 열린 창([dialog]) 하나를 그립니다. */
@Composable
internal fun SettingsDialogs(dialog: SettingsDialog?, state: SettingsUiState, caps: Capabilities, onEvent: (SettingsEvent) -> Unit, onDismiss: () -> Unit) {
    when (dialog) {
        null -> Unit
        SettingsDialog.SignOut ->
            ConfirmDialog("연결 해제", "정말 이 기기에서 연결을 해제할까요?", confirmLabel = "해제", onConfirm = { onEvent(SettingsEvent.SignOut) }, onDismiss = onDismiss)
        is SettingsDialog.RemoveMember -> {
            val name = state.members.firstOrNull { it.id == dialog.id }?.name ?: ""
            ConfirmDialog(
                "연결 끊기", "'$name' 님을 구성원 목록에서 제거할까요? 상대 기기에서는 다시 코드를 입력해야 연결됩니다.", confirmLabel = "제거",
                onConfirm = { onEvent(SettingsEvent.RemoveMember(dialog.id)) }, onDismiss = onDismiss,
            )
        }
        SettingsDialog.AddChild -> AddChildDialog(onConfirm = { name, birth -> onEvent(SettingsEvent.AddChild(name, birth)) }, onDismiss = onDismiss)
        SettingsDialog.EditYear -> StudentYearDialog(
            birthDate = state.birthDate, ageLabel = state.ageLabel, gradeYear = state.student?.gradeYear ?: 0,
            auto = state.autoStudentLevel, chosen = state.chosenStudentLevel, canChooseLevel = caps.canChooseStudentScreen,
            onSave = { birth, grade, level -> onEvent(SettingsEvent.SaveStudentYear(birth, grade, level)); onDismiss() },
            onDismiss = onDismiss,
        )
        SettingsDialog.LinkChild ->
            TextInputDialog(title = "코드로 연결", label = "연결 코드 6자리", confirmLabel = "연결", onConfirm = { onEvent(SettingsEvent.LinkChild(it)) }, onDismiss = onDismiss)
        SettingsDialog.MyInfo -> DetailSheet("내 정보", onDismiss = onDismiss) {
            MyInfoCard(state.profile, state.me, showsRelation = caps.isParent, onRelation = { me, label -> onEvent(SettingsEvent.UpdateMyProfile(me.name, label)) })
        }
        SettingsDialog.PairingCode -> DetailSheet("연결 코드", onDismiss = onDismiss) {
            PairingCodeCard(code = state.profile?.pairingCode, isStudent = caps.isStudent, syncStatus = state.syncStatus, syncAvailable = state.syncAvailable, onRequestSync = { onEvent(SettingsEvent.RequestSync) })
        }
        SettingsDialog.School -> SchoolSheet(
            current = state.student?.schoolName.orEmpty(), search = state.schoolSearch,
            onSearch = { onEvent(SettingsEvent.SearchSchool(it)) }, onPick = { onEvent(SettingsEvent.PickSchool(it)) },
            onSync = { onEvent(SettingsEvent.SyncSchool) }, onDismiss = onDismiss,
        )
        SettingsDialog.Signature -> TextInputDialog(
            title = "리포트 서명", label = "예: 김쌤 수학 · 010-0000-0000", initial = state.me?.signature.orEmpty(),
            hint = "수업 리포트 끝에 붙어요", onConfirm = { onEvent(SettingsEvent.SetSignature(it)) }, onDismiss = onDismiss,
        )
        SettingsDialog.Subjects ->
            SubjectSelectDialog(state.subjects, state.me?.subjectIdList ?: emptyList(), onDismiss = onDismiss) { onEvent(SettingsEvent.SetMySubjects(it)) }
    }
}
