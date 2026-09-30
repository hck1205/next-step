package com.nextstep.app.ui.components.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 크게 누르는 고르기 카드: 두 줄 격자(그림 위, 말 아래). [wide] 면 한 줄에 하나(그림 옆에 말).
 * 고른 것을 다시 누르면 비웁니다([onPick] 에 빈 문자열).
 */
@Composable
fun OptionCardGrid(options: List<String>, iconOf: (String) -> ImageVector, selected: String, onPick: (String) -> Unit, wide: Boolean = false) {
    val rows = if (wide) options.map { listOf(it) } else options.chunked(COLUMNS)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { o -> OptionCard(o, iconOf(o), o == selected, wide, Modifier.weight(1f)) { onPick(if (o == selected) "" else o) } }
                if (!wide && row.size < COLUMNS) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun OptionCard(text: String, icon: ImageVector, on: Boolean, wide: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier.heightIn(min = if (wide) 0.dp else 104.dp).background(if (on) cs.primaryContainer else cs.surface, shape)
            .border(if (on) 2.dp else 1.dp, if (on) cs.primary else cs.outlineVariant, shape)
            .selectable(selected = on, role = Role.RadioButton, onClick = onClick).padding(14.dp),
    ) {
        val badge = @Composable {
            Box(Modifier.size(40.dp).background(if (on) cs.primary else cs.surfaceVariant, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = if (on) cs.onPrimary else cs.onSurfaceVariant, modifier = Modifier.size(22.dp))
            }
        }
        val label = @Composable { Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = cs.onSurface) }
        if (wide) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { badge(); label() }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { badge(); label() }
        }
        if (on) Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = cs.primary, modifier = Modifier.align(Alignment.TopEnd).size(20.dp))
    }
}

private const val COLUMNS = 2
