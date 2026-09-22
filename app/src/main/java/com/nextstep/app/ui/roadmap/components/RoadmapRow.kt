package com.nextstep.app.ui.roadmap.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.SubjectTag

@Composable
internal fun RoadmapRow(item: RoadmapItemEntity, subjects: List<SubjectEntity>, caps: Capabilities, linked: ContentEntity?, onStatus: (RoadmapStatus) -> Unit, onEdit: () -> Unit, onOpenLinked: (ContentEntity) -> Unit) {
    val subject = subjects.firstOrNull { it.id == item.subjectId }
    val done = item.status == RoadmapStatus.DONE
    val overdue = !done && item.targetDate != null && item.targetDate < DateUtils.today().toEpochDay()
    AppCard(onClick = if (caps.canEditRoadmap) onEdit else null) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (done) { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary); Spacer(Modifier.width(6.dp)) }
                Text(item.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), textDecoration = if (done) TextDecoration.LineThrough else null)
                Text(
                    item.status.label, style = MaterialTheme.typography.labelSmall,
                    color = when (item.status) { RoadmapStatus.PLANNED -> MaterialTheme.colorScheme.onSurfaceVariant; RoadmapStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary; RoadmapStatus.DONE -> MaterialTheme.colorScheme.secondary },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                SubjectTag(subject)
                item.targetDate?.let {
                    Text(
                        "${DateUtils.formatDate(DateUtils.fromEpochDay(it))}까지" + (if (overdue) " · 지남" else " · ${DateUtils.dDay(DateUtils.fromEpochDay(it))}"),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (item.createdByName.isNotBlank()) Text("${item.createdByName} 제안", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (item.description.isNotBlank()) Text(item.description, style = MaterialTheme.typography.bodySmall)
            if (linked != null) {
                TextButton(onClick = { onOpenLinked(linked) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("영상: ${linked.title}", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (item.resource.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    Text(item.resource, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            if (caps.canUpdateRoadmapProgress && !done) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RoadmapStatus.entries.forEach { s ->
                        FilterChip(selected = item.status == s, onClick = { onStatus(s) }, label = { Text(s.label, style = MaterialTheme.typography.labelSmall) })
                    }
                }
            } else if (caps.canUpdateRoadmapProgress && done) {
                TextButton(onClick = { onStatus(RoadmapStatus.IN_PROGRESS) }) { Text("다시 진행 중으로") }
            }
        }
    }
}
