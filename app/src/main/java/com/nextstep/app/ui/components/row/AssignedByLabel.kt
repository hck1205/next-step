package com.nextstep.app.ui.components.row

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.Role

/** 학생이 직접 만든 할 일이 아니면 "학부모 배정"처럼 누가 냈는지 표시합니다. */
@Composable
fun AssignedByLabel(task: TaskEntity) {
    if (task.isStudentMade) return
    Text("${Role.labelOf(task.createdByRole)} 배정", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
}
