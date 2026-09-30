package com.nextstep.app.ui.onboarding.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.model.Role

@Composable
internal fun RoleStep(onSelect: (Role) -> Unit) {
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
