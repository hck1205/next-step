package com.nextstep.app.ui.curriculum.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.curriculum.UnitPlan
import com.nextstep.app.domain.curriculum.UnitStatus

/** 단원 한 줄: 상태, 뼈대 표시, 또래 수, 제안, 영상 2개 또는 유튜브 검색, 할 일로 보내기. */
@Composable
fun UnitRow(plan: UnitPlan, onAddTask: (() -> Unit)?, onOpenUrl: (String) -> Unit, onWatched: (String) -> Unit, modifier: Modifier = Modifier) {
    val statusColor = when (plan.status) {
        UnitStatus.DONE -> MaterialTheme.colorScheme.secondary
        UnitStatus.IN_CLASS -> MaterialTheme.colorScheme.primary
        UnitStatus.REGISTERED -> MaterialTheme.colorScheme.tertiary
        UnitStatus.NOT_REGISTERED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(plan.unit.title, style = MaterialTheme.typography.bodyLarge, fontWeight = if (plan.unit.essential) FontWeight.SemiBold else FontWeight.Normal)
                Text(
                    listOfNotNull(plan.status.label, "뼈대 단원".takeIf { plan.unit.essential }, plan.peerFamilies?.let { "다른 가족 ${it}곳" }).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall, color = statusColor,
                )
            }
            if (onAddTask != null && plan.status != UnitStatus.DONE) TextButton(onClick = onAddTask) { Text("할 일로") }
        }
        plan.suggestion?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
        if (plan.videos.isEmpty()) {
            TextButton(onClick = { onOpenUrl(plan.searchUrl) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) { Text("유튜브에서 개념 영상 찾기") }
        } else {
            Spacer(Modifier.height(2.dp))
            plan.videos.forEach { v ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { onOpenUrl(v.url) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp), modifier = Modifier.weight(1f)) {
                        Text("▶ ${v.title}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                    TextButton(onClick = { onWatched(v.id) }) { Text("봤어요") }
                }
            }
        }
    }
}
