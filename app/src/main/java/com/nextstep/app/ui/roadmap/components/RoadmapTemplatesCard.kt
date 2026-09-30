package com.nextstep.app.ui.roadmap.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.RoadmapTemplateEntity
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.dialog.TextInputDialog

/**
 * 로드맵 템플릿(멘토): 지금 로드맵을 순서·간격 그대로 저장하고, 다른 학생을 열어 "불러오기"로 오늘부터 붙입니다.
 * 템플릿은 이 기기에만 있는 멘토의 서랍입니다.
 */
@Composable
internal fun RoadmapTemplatesCard(templates: List<RoadmapTemplateEntity>, canSave: Boolean, onSave: (String) -> Unit, onApply: (RoadmapTemplateEntity) -> Unit, onDelete: (String) -> Unit) {
    var naming by remember { mutableStateOf(false) }
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { naming = true }, enabled = canSave) { Text("지금 로드맵을 템플릿으로 저장") }
            if (templates.isEmpty()) Text("저장한 템플릿이 없어요. 다른 학생에게 같은 순서로 가르칠 때 불러와요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            templates.forEach { t ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(t.name, style = MaterialTheme.typography.titleSmall)
                        Text(listOf(t.subjectName, "${t.count}개").filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { onApply(t) }) { Text("불러오기") }
                    IconButton(onClick = { onDelete(t.id) }) { Icon(Icons.Default.Delete, contentDescription = "${t.name} 지우기") }
                }
            }
        }
    }
    if (naming) TextInputDialog(title = "템플릿 이름", label = "예: 초5 수학 1학기 기본", onConfirm = { onSave(it) }, onDismiss = { naming = false })
}
