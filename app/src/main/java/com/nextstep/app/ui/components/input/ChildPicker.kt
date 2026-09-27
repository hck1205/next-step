package com.nextstep.app.ui.components.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.prefs.LinkedChild

/**
 * 상단 바의 자녀(학생) 고르기: 지금 보는 아이 이름 ▾ → 누르면 다른 아이와 "자녀 추가". 본문 맨 위에 있던 자녀 줄을 머리로 옮긴 것입니다.
 * 아이가 하나뿐이고 추가할 곳도 없으면 그리지 않습니다.
 */
@Composable
fun ChildPicker(children: List<LinkedChild>, activeFamilyId: String?, onSelect: (String) -> Unit, onAdd: (() -> Unit)?) {
    if (children.size < 2 && onAdd == null) return
    var open by remember { mutableStateOf(false) }
    val active = children.firstOrNull { it.familyId == activeFamilyId } ?: children.firstOrNull()
    Box {
        AssistChip(
            onClick = { open = true },
            label = { Text(active?.studentName?.ifBlank { null } ?: "자녀") },
            leadingIcon = { Initial(active?.studentName.orEmpty()) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "자녀 바꾸기") },
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            children.forEach { child ->
                val selected = child.familyId == active?.familyId
                DropdownMenuItem(
                    text = { Text(child.studentName.ifBlank { "자녀" }) },
                    leadingIcon = { Initial(child.studentName) },
                    trailingIcon = { if (selected) Icon(Icons.Default.Check, contentDescription = "지금 보는 아이") },
                    onClick = { open = false; if (!selected) onSelect(child.familyId) },
                )
            }
            if (onAdd != null) DropdownMenuItem(text = { Text("자녀 추가·연결") }, leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }, onClick = { open = false; onAdd() })
        }
    }
}

@Composable
private fun Initial(name: String) {
    Box(Modifier.size(22.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
        Text(name.take(1), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary)
    }
}
