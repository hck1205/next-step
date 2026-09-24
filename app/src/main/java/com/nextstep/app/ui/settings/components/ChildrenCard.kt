package com.nextstep.app.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.prefs.LinkedChild
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.input.ChildSwitcher

/** 연결된 자녀(또는 멘토가 맡은 학생) 목록과 추가 버튼. 자녀마다 기록·여정이 따로 쌓입니다. */
@Composable
internal fun ChildrenCard(
    children: List<LinkedChild>,
    activeFamilyId: String?,
    error: String?,
    onSelect: (String) -> Unit,
    onAdd: (() -> Unit)?,
    onLink: (() -> Unit)?,
) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ChildSwitcher(children, activeFamilyId, onSelect = onSelect)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                onAdd?.let { OutlinedButton(onClick = it, modifier = Modifier.weight(1f)) { Text("새 자녀") } }
                onLink?.let { OutlinedButton(onClick = it, modifier = Modifier.weight(1f)) { Text("코드로 연결") } }
            }
            if (error != null) Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}
