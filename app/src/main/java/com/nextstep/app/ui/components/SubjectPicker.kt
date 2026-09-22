package com.nextstep.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/** 과목 선택 드롭다운. allowNone 이면 "과목 없음" 항목을 포함합니다. */
@Composable
fun SubjectPicker(subjects: List<SubjectEntity>, selectedId: String?, onSelect: (String?) -> Unit, allowNone: Boolean = true, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val selected = subjects.firstOrNull { it.id == selectedId }
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            if (selected != null) ColorDot(subjectColor(selected.color))
            Spacer(Modifier.width(6.dp))
            Text(selected?.name ?: "과목 선택", modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (allowNone) DropdownMenuItem(text = { Text("과목 없음") }, onClick = { onSelect(null); expanded = false })
            subjects.forEach { s ->
                DropdownMenuItem(
                    text = { Row(verticalAlignment = Alignment.CenterVertically) { ColorDot(subjectColor(s.color)); Spacer(Modifier.width(8.dp)); Text(s.name) } },
                    onClick = { onSelect(s.id); expanded = false },
                )
            }
        }
    }
}
