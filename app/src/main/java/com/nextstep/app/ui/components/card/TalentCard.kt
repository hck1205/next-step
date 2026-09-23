package com.nextstep.app.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.insight.Talent

@Composable
fun TalentCard(talent: Talent, subjects: List<SubjectEntity>) {
    val subject = subjects.firstOrNull { it.id == talent.subjectId }
    val color = MaterialTheme.colorScheme.tertiary
    AppCard {
        Row(verticalAlignment = Alignment.Top) {
            Box(Modifier.background(color.copy(alpha = 0.12f), MaterialTheme.shapes.small).padding(8.dp)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "재능", tint = color)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("강점 신호 ${"★".repeat((talent.strength * 3).toInt().coerceIn(1, 3))}", style = MaterialTheme.typography.labelSmall, color = color)
                    if (subject != null) SubjectTag(subject)
                }
                Text(talent.title, style = MaterialTheme.typography.titleSmall)
                Text(talent.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
