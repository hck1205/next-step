package com.nextstep.app.ui.components.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.prefs.LinkedChild

/**
 * 다자녀 전환 줄: 자녀마다 이니셜 원 + 이름 칩. 누르면 그 자녀의 공간으로 바뀝니다.
 * [onAdd] 가 있으면 끝에 "+ 자녀" 칩을 둡니다.
 */
@Composable
fun ChildSwitcher(children: List<LinkedChild>, activeFamilyId: String?, onSelect: (String) -> Unit, onAdd: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    LazyRow(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(children, key = { it.familyId }) { child ->
            val selected = child.familyId == activeFamilyId
            FilterChip(
                selected = selected,
                onClick = { if (!selected) onSelect(child.familyId) },
                label = { Text(child.studentName.ifBlank { "자녀" }) },
                leadingIcon = {
                    Box(Modifier.size(22.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                        Text(child.studentName.take(1), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
            )
        }
        if (onAdd != null) item(key = "add") {
            FilterChip(selected = false, onClick = onAdd, label = { Text("자녀") }, leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) })
        }
    }
}
