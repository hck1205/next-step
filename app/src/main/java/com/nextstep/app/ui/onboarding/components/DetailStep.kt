package com.nextstep.app.ui.onboarding.components

import com.nextstep.app.ui.settings.components.RelationPicker
import com.nextstep.app.domain.time.DateUtils
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.model.Role
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.input.DateField
import com.nextstep.app.ui.components.input.GradePicker
import com.nextstep.app.ui.onboarding.OnboardingEvent
import com.nextstep.app.ui.onboarding.OnboardingUiState
import java.time.LocalDate

@Composable
internal fun DetailStep(state: OnboardingUiState, onEvent: (OnboardingEvent) -> Unit) {
    val role = state.role ?: Role.STUDENT
    val isStudent = role == Role.STUDENT
    val isMentor = role == Role.MENTOR
    val isParent = role == Role.PARENT
    val parentCreates = isParent && state.createAsParent
    Text("${role.label} 정보", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = state.name,
        onValueChange = { onEvent(OnboardingEvent.SetName(it)) },
        label = { Text(when (role) { Role.STUDENT -> "학생 이름"; Role.PARENT -> "이름"; Role.MENTOR -> "이름 (예: 김선생)" }) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    if (isStudent) {
        Spacer(Modifier.height(12.dp))
        BirthDateField(state, onEvent)
        Spacer(Modifier.height(8.dp))
        GradePicker(gradeYear = state.gradeYear, onSelect = { onEvent(OnboardingEvent.SetGrade(it)) })
        Spacer(Modifier.height(4.dp))
        Text("생년월일과 학년에 따라 여정 타임라인, 추천 영상, 학습 계획 길이, 부모님·멘토 가이드가 달라져요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (isParent) {
        Spacer(Modifier.height(12.dp))
        Text("아이와의 관계", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        RelationPicker(state.relation, onSelect = { onEvent(OnboardingEvent.SetRelation(it)) })
        Text("엄마·아빠·할머니 등 여러 보호자가 같은 아이에 함께 연결될 수 있어요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("자녀가 아직 앱을 쓰지 않아요", style = MaterialTheme.typography.bodyMedium)
                Text("영유아·초등 저학년이면 부모가 먼저 시작하고, 나중에 자녀 기기를 연결 코드로 붙일 수 있어요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = state.createAsParent, onCheckedChange = { onEvent(OnboardingEvent.SetCreateAsParent(it)) })
        }
        if (state.createAsParent) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.childName,
                onValueChange = { onEvent(OnboardingEvent.SetChildName(it)) },
                label = { Text("자녀 이름") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            BirthDateField(state, onEvent)
        }
    }
    if (isMentor) {
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.title,
            onValueChange = { onEvent(OnboardingEvent.SetTitle(it)) },
            label = { Text("구분 (예: 수학 과외, 담임 선생님, 영어 튜터)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    if (!isStudent && !parentCreates) {
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.code,
            onValueChange = { onEvent(OnboardingEvent.SetCode(it)) },
            label = { Text("학생 연결 코드 (6자리)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Ascii),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (isMentor) "학생(또는 학부모) 앱의 설정 화면에 표시된 연결 코드를 입력하세요. 연결 후 담당 과목을 고를 수 있어요."
            else "자녀의 앱 → 설정 화면에 표시된 연결 코드를 입력하세요.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else if (isStudent && !state.syncAvailable) {
        Spacer(Modifier.height(8.dp))
        AppCard {
            Text(
                "동기화 서버(Firebase)가 설정되지 않아 이 기기에만 저장됩니다. 학부모 연동을 쓰려면 README 의 Firebase 설정을 완료하세요.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    state.error?.let {
        Spacer(Modifier.height(8.dp))
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
    Spacer(Modifier.height(24.dp))
    Button(onClick = { onEvent(OnboardingEvent.Submit) }, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
        if (state.loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
        else Text(when { role == Role.STUDENT -> "시작하기"; parentCreates -> "자녀의 여정 시작하기"; role == Role.PARENT -> "자녀와 연결하기"; else -> "학생과 연결하기" })
    }
    TextButton(onClick = { onEvent(OnboardingEvent.Back) }, modifier = Modifier.fillMaxWidth()) { Text("역할 다시 선택") }
}

/** 생년월일 입력. 비어 있으면 "선택" 버튼, 있으면 날짜 필드와 지우기. */
@Composable
private fun BirthDateField(state: OnboardingUiState, onEvent: (OnboardingEvent) -> Unit) {
    val birth = state.birthDate
    if (birth == null) {
        OutlinedButton(onClick = { onEvent(OnboardingEvent.SetBirthDate(DateUtils.today().minusYears(3))) }, modifier = Modifier.fillMaxWidth()) { Text("생년월일 입력 (여정 타임라인에 필요해요)") }
    } else {
        DateField(label = "생년월일", date = birth, onChange = { onEvent(OnboardingEvent.SetBirthDate(it)) })
        TextButton(onClick = { onEvent(OnboardingEvent.SetBirthDate(null)) }) { Text("생년월일 지우기") }
    }
}
