package com.nextstep.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.stats.EventOccurrence
import com.nextstep.app.domain.time.DateUtils

@Composable
fun EventRow(occ: EventOccurrence, subjects: List<SubjectEntity>) {
    val subject = subjects.firstOrNull { it.id == occ.event.subjectId }
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.width(56.dp)) {
                Text(DateUtils.formatTime(occ.startAt), style = MaterialTheme.typography.titleSmall)
                Text(DateUtils.formatTime(occ.endAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(occ.event.title, style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(occ.event.type.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (subject != null) SubjectTag(subject)
                    if (occ.event.location.isNotBlank()) Text(occ.event.location, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
