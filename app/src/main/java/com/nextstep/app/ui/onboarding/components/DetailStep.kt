package com.nextstep.app.ui.onboarding.components

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
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.input.DateField
import com.nextstep.app.ui.components.input.GradePicker
import com.nextstep.app.ui.onboarding.OnboardingEvent
import com.nextstep.app.ui.onboarding.OnboardingUiState
import com.nextstep.app.ui.settings.components.RelationPicker

/**
 * 역할을 고른 다음 단계: 이름 → 역할별 칸(학생 생년월일·학년, 학부모 관계·직접 만들기, 멘토 구분) → 연결 코드 → 시작.
 * 온보딩은 역할을 정하는 화면이라 아직 권한(caps)이 없고, 어떤 칸을 보일지는 고른 역할이 정합니다.
 */
@Composable
internal fun DetailStep(state: OnboardingUiState, onEvent: (OnboardingEvent) -> Unit) {
    Text("${state.shownRole.label} 정보", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(16.dp))
    SingleLineField(state.name, state.nameLabel) { onEvent(OnboardingEvent.SetName(it)) }
    Spacer(Modifier.height(12.dp))
    when (state.shownRole) {
        Role.STUDENT -> StudentFields(state, onEvent)
        Role.PARENT -> ParentFields(state, onEvent)
        Role.MENTOR -> SingleLineField(state.title, "구분 (예: 수학 과외, 담임 선생님, 영어 튜터)") { onEvent(OnboardingEvent.SetTitle(it)) }
    }
    if (state.joinsWithCode) CodeField(state, onEvent)
    else if (state.shownRole == Role.STUDENT && !state.syncAvailable) LocalOnlyNote()
    state.error?.let {
        Spacer(Modifier.height(8.dp))
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
    Spacer(Modifier.height(24.dp))
    Button(onClick = { onEvent(OnboardingEvent.Submit) }, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
        if (state.loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
        else Text(state.submitLabel)
    }
    TextButton(onClick = { onEvent(OnboardingEvent.Back) }, modifier = Modifier.fillMaxWidth()) { Text("역할 다시 선택") }
}

/** 학생: 생년월일 · 학년. 둘에 따라 여정·추천·계획 길이가 달라집니다. */
@Composable
private fun StudentFields(state: OnboardingUiState, onEvent: (OnboardingEvent) -> Unit) {
    BirthDateField(state, onEvent)
    Spacer(Modifier.height(8.dp))
    GradePicker(gradeYear = state.gradeYear, onSelect = { onEvent(OnboardingEvent.SetGrade(it)) })
    Spacer(Modifier.height(4.dp))
    Hint("생년월일과 학년에 따라 여정 타임라인, 추천 영상, 학습 계획 길이, 부모님·멘토 가이드가 달라져요.")
}

/** 학부모: 아이와의 관계, 그리고 자녀 기기 없이 직접 시작할지(그러면 자녀 이름·생년월일). */
@Composable
private fun ParentFields(state: OnboardingUiState, onEvent: (OnboardingEvent) -> Unit) {
    Text("아이와의 관계", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    RelationPicker(state.relation, onSelect = { onEvent(OnboardingEvent.SetRelation(it)) })
    Hint("엄마·아빠·할머니 등 여러 보호자가 같은 아이에 함께 연결될 수 있어요.")
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("자녀가 아직 앱을 쓰지 않아요", style = MaterialTheme.typography.bodyMedium)
            Hint("영유아·초등 저학년이면 부모가 먼저 시작하고, 나중에 자녀 기기를 연결 코드로 붙일 수 있어요.")
        }
        Switch(checked = state.createAsParent, onCheckedChange = { onEvent(OnboardingEvent.SetCreateAsParent(it)) })
    }
    if (state.createAsParent) {
        Spacer(Modifier.height(8.dp))
        SingleLineField(state.childName, "자녀 이름") { onEvent(OnboardingEvent.SetChildName(it)) }
        Spacer(Modifier.height(8.dp))
        BirthDateField(state, onEvent)
    }
}

/** 이미 있는 가족에 들어갈 때의 연결 코드 6자리. */
@Composable
private fun CodeField(state: OnboardingUiState, onEvent: (OnboardingEvent) -> Unit) {
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
    Hint(state.codeHint)
}

/** 동기화 서버가 없을 때 학생에게: 이 기기에만 저장된다는 안내. */
@Composable
private fun LocalOnlyNote() {
    Spacer(Modifier.height(8.dp))
    AppCard { Hint("동기화 서버(Firebase)가 설정되지 않아 이 기기에만 저장됩니다. 학부모 연동을 쓰려면 README 의 Firebase 설정을 완료하세요.") }
}

@Composable
private fun SingleLineField(value: String, label: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) }, singleLine = true, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun Hint(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
