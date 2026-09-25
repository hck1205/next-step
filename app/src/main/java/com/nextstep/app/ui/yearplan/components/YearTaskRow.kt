package com.nextstep.app.ui.yearplan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.yearplan.YearTaskView

/**
 * 올해 할 일 한 줄: 동그라미 체크(누르면 완료/되돌리기) + 제목 + 방법 한 줄. 줄을 누르면 자세히.
 * [minHeightDp] 는 화면 단계의 누름 영역, [showArea] 는 전체 탭에서 분류 이름을 붙일 때.
 */
@Composable
internal fun YearTaskRow(view: YearTaskView, minHeightDp: Int, showArea: Boolean, onToggle: () -> Unit, onOpen: () -> Unit) {
    AppCard(modifier = Modifier.heightIn(min = minHeightDp.dp), onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val circle = Modifier.size((minHeightDp - CIRCLE_INSET).coerceAtLeast(MIN_CIRCLE).dp)
            Box(
                (if (view.done) circle.background(MaterialTheme.colorScheme.secondary, CircleShape) else circle.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape))
                    .clickable(onClick = onToggle)
                    .semantics { role = Role.Checkbox; stateDescription = if (view.done) "했어요" else "아직" },
                contentAlignment = Alignment.Center,
            ) { if (view.done) Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary) }
            Column(Modifier.weight(1f)) {
                Text(
                    view.task.title, style = MaterialTheme.typography.titleSmall,
                    textDecoration = if (view.done) TextDecoration.LineThrough else null,
                    color = if (view.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    (if (showArea) "${view.task.area.label} · " else "") + view.task.how,
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2,
                )
            }
        }
    }
}

private const val CIRCLE_INSET = 16
private const val MIN_CIRCLE = 28
