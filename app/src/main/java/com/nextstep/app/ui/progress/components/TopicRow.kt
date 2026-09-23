package com.nextstep.app.ui.progress.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.dialog.ConfirmDialog
import com.nextstep.app.ui.components.dialog.TextInputDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
internal fun TopicRow(
    topic: TopicEntity,
    caps: Capabilities,
    onToggleCovered: () -> Unit,
    onStatus: (TopicStatus) -> Unit,
    onConfidence: (Int) -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onAddTask: (TaskType) -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    var rename by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    AppCard(onClick = { expanded = !expanded }) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = topic.classCovered, onCheckedChange = { onToggleCovered() }, enabled = caps.canEditTopics)
                Column(Modifier.weight(1f)) {
                    Text(topic.title, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        topic.status.label + (if (topic.confidence > 0) " · 이해도 ${topic.confidence}%" else ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (topic.status) {
                            TopicStatus.NOT_STARTED -> MaterialTheme.colorScheme.onSurfaceVariant
                            TopicStatus.PREVIEWED, TopicStatus.IN_CLASS -> MaterialTheme.colorScheme.primary
                            TopicStatus.REVIEWED, TopicStatus.MASTERED -> MaterialTheme.colorScheme.secondary
                        },
                    )
                }
                if (caps.canCreateTasks || caps.canEditTopics) IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "메뉴") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    if (caps.canCreateTasks) {
                        DropdownMenuItem(text = { Text(if (caps.isStudent) "예습 할 일 추가" else "예습 과제 배정") }, onClick = { onAddTask(TaskType.PREVIEW); menu = false })
                        DropdownMenuItem(text = { Text(if (caps.isStudent) "복습 할 일 추가" else "복습 과제 배정") }, onClick = { onAddTask(TaskType.REVIEW); menu = false })
                    }
                    if (caps.canEditTopics) {
                        DropdownMenuItem(text = { Text("이름 변경") }, onClick = { rename = true; menu = false })
                        DropdownMenuItem(text = { Text("삭제") }, leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }, onClick = { confirmDelete = true; menu = false })
                    }
                }
            }
            if (expanded && caps.canMarkTopicStatus) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                    TopicStatus.entries.forEach { s ->
                        FilterChip(selected = topic.status == s, onClick = { onStatus(s) }, label = { Text(s.label, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("이해도 ${topic.confidence}%  (슬라이더를 놓으면 저장)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                var conf by remember(topic.confidence) { mutableStateOf(topic.confidence.toFloat()) }
                Slider(
                    value = conf,
                    onValueChange = { conf = it },
                    onValueChangeFinished = { onConfidence(conf.toInt()) },
                    valueRange = 0f..100f,
                    steps = 9,
                )
            }
        }
    }
    if (rename) TextInputDialog("단원 이름 변경", "단원명", topic.title, onConfirm = onRename, onDismiss = { rename = false })
    if (confirmDelete) ConfirmDialog("단원 삭제", "'${topic.title}' 단원을 삭제할까요?", "삭제", onConfirm = onDelete, onDismiss = { confirmDelete = false })
}
