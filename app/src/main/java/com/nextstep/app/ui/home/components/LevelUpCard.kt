package com.nextstep.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.growth.StudentHomeSection
import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.ui.components.card.AppCard

/** 학년이 올라 화면 단계가 바뀐 첫날의 카드: 새 단계 이름과 새로 생긴 카드들. "좋아요"로 닫습니다. */
@Composable
internal fun LevelUpCard(level: StudentUiLevel, opened: List<StudentHomeSection>, onOk: () -> Unit) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Column {
                    Text("한 뼘 자랐어요", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text("${level.label} 화면이 열렸어요", style = MaterialTheme.typography.titleMedium)
                }
            }
            opened.forEach { section ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Text(section.label, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Button(onClick = onOk, modifier = Modifier.fillMaxWidth()) { Text("좋아요") }
        }
    }
}
