package com.nextstep.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.growth.StudyKind
import com.nextstep.app.domain.growth.StudyKindType
import com.nextstep.app.domain.growth.YearProfile
import com.nextstep.app.ui.components.card.AppCard

/**
 * 올해의 공부: 그 해(만 나이·학년)에 하는 공부 종류와 권장 양, 올해 과목. 해마다 내용이 바뀝니다.
 * 한 줄의 + 를 누르면 그 분량이 오늘 할 일이 됩니다. 과목별로 잘게 나눈 올해 할 일은 "올해" 탭에 있습니다.
 */
@Composable
internal fun YearCard(year: YearProfile, onAdd: (StudyKind) -> Unit, onOpenYear: () -> Unit) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("${year.label} · 올해의 공부", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(year.theme, style = MaterialTheme.typography.titleSmall)
            year.kinds.forEach { kind ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(kindIcon(kind.type), contentDescription = kind.type.label, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    Column(Modifier.weight(1f)) {
                        Text(kind.name, style = MaterialTheme.typography.bodyLarge)
                        Text(kind.amountLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onAdd(kind) }) { Icon(Icons.Default.AddCircle, contentDescription = "${kind.name} 오늘 할 일로", tint = MaterialTheme.colorScheme.primary) }
                }
            }
            Text(
                (if (year.dailyMinutes == 0) "앉아서 하는 공부 없이 놀이로" else "하루 ${year.dailyMinutes}분이면 충분해요") + " · " + year.subjects.joinToString(" · "),
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2,
            )
            TextButton(onClick = onOpenYear) { Text("올해 할 일 모두 보기") }
        }
    }
}

private fun kindIcon(type: StudyKindType): ImageVector = when (type) {
    StudyKindType.PLAY -> Icons.Default.Toys
    StudyKindType.READ -> Icons.AutoMirrored.Filled.MenuBook
    StudyKindType.WRITE -> Icons.Default.Edit
    StudyKindType.PRACTICE -> Icons.Default.Calculate
    StudyKindType.TEST_PREP -> Icons.Default.Quiz
    StudyKindType.PROJECT -> Icons.Default.Science
    StudyKindType.CAREER -> Icons.Default.Explore
    StudyKindType.HABIT -> Icons.Default.CheckCircle
}
