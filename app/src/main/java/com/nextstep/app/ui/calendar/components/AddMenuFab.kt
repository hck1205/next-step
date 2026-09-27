package com.nextstep.app.ui.calendar.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/** + 를 누르면 일정 추가, 그리고 할 일을 만들 수 있으면([taskLabel] 있음) 할 일 추가. */
@Composable
internal fun AddMenuFab(taskLabel: String?, onAddEvent: () -> Unit, onAddTask: () -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        FloatingActionButton(onClick = { open = true }) { Icon(Icons.Default.Add, contentDescription = "추가") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text("일정 추가") }, onClick = { onAddEvent(); open = false })
            if (taskLabel != null) DropdownMenuItem(text = { Text(taskLabel) }, onClick = { onAddTask(); open = false })
        }
    }
}
