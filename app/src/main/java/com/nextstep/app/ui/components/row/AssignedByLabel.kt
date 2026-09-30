package com.nextstep.app.ui.components.row

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.domain.goaltree.Assigner

/** 어른이 준 할 일이면 "학부모가 준 일"처럼 누가 냈는지 표시합니다(문구는 [Assigner] 한 곳). */
@Composable
fun AssignedByLabel(task: TaskEntity) {
    val by = Assigner.givenBy(task.createdByRole) ?: return
    Text(by.taskLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
}
