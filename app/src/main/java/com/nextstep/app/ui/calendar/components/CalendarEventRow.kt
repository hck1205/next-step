package com.nextstep.app.ui.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.model.EventType
import com.nextstep.app.domain.stats.EventOccurrence
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.SubjectTag

@Composable
internal fun CalendarEventRow(occ: EventOccurrence, subjects: List<SubjectEntity>, onClick: () -> Unit) {
    val subject = subjects.firstOrNull { it.id == occ.event.subjectId }
    val typeColor = when (occ.event.type) {
        EventType.EXAM -> MaterialTheme.colorScheme.error
        EventType.CLASS -> MaterialTheme.colorScheme.primary
        EventType.ACADEMY -> MaterialTheme.colorScheme.tertiary
        EventType.STUDY -> MaterialTheme.colorScheme.secondary
        EventType.OTHER -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    AppCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 4.dp, height = 36.dp).background(typeColor, CircleShape))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.width(52.dp)) {
                Text(DateUtils.formatTime(occ.startAt), style = MaterialTheme.typography.titleSmall)
                Text(DateUtils.formatTime(occ.endAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(occ.event.title, style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(occ.event.type.label + (if (occ.event.repeatWeekly) " · 매주" else ""), style = MaterialTheme.typography.labelSmall, color = typeColor)
                    if (subject != null) SubjectTag(subject)
                    if (occ.event.location.isNotBlank()) Text(occ.event.location, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
