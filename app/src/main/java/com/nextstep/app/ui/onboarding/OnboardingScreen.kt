package com.nextstep.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.model.Role
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AppCard

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("NextStep", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text("학업 스케줄과 진도를 학생·학부모가 함께 관리해요", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))

        when (state.step) {
            0 -> RoleStep(onSelect = viewModel::selectRole)
            else -> DetailStep(state, viewModel)
        }
    }
}

@Composable
private fun RoleStep(onSelect: (Role) -> Unit) {
    Text("어떤 역할로 사용하시나요?", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(16.dp))
    RoleCard(
        title = Role.STUDENT.label,
        desc = Role.STUDENT.description,
        icon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary) },
        onClick = { onSelect(Role.STUDENT) },
    )
    Spacer(Modifier.height(12.dp))
    RoleCard(
        title = Role.PARENT.label,
        desc = Role.PARENT.description,
        icon = { Icon(Icons.Default.FamilyRestroom, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.secondary) },
        onClick = { onSelect(Role.PARENT) },
    )
    Spacer(Modifier.height(12.dp))
    RoleCard(
        title = "${Role.MENTOR.label} (선생님·과외·튜터)",
        desc = Role.MENTOR.description + " 한 학생에 여러 멘토가 연결될 수 있어요.",
        icon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.tertiary) },
        onClick = { onSelect(Role.MENTOR) },
    )
}

@Composable
private fun RoleCard(title: String, desc: String, icon: @Composable () -> Unit, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            icon()
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DetailStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    val role = state.role ?: Role.STUDENT
    val isStudent = role == Role.STUDENT
    val isMentor = role == Role.MENTOR
    Text("${role.label} 정보", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = state.name,
        onValueChange = viewModel::setName,
        label = { Text(when (role) { Role.STUDENT -> "학생 이름"; Role.PARENT -> "이름 (예: 엄마, 아빠)"; Role.MENTOR -> "이름 (예: 김선생)" }) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    if (isMentor) {
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.title,
            onValueChange = viewModel::setTitle,
            label = { Text("구분 (예: 수학 과외, 담임 선생님, 영어 튜터)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    if (!isStudent) {
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.code,
            onValueChange = viewModel::setCode,
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
    } else if (!state.syncAvailable) {
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
    Button(onClick = viewModel::submit, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
        if (state.loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
        else Text(when (role) { Role.STUDENT -> "시작하기"; Role.PARENT -> "자녀와 연결하기"; Role.MENTOR -> "학생과 연결하기" })
    }
    TextButton(onClick = viewModel::back, modifier = Modifier.fillMaxWidth()) { Text("역할 다시 선택") }
}
