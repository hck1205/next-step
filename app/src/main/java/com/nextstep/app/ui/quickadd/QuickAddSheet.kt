package com.nextstep.app.ui.quickadd

import com.nextstep.app.ui.quickadd.components.KidRecordGrid
import com.nextstep.app.domain.growth.StudentUiLevel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.activities.components.ActivityEditDialog
import com.nextstep.app.ui.components.dialog.EventEditDialog
import com.nextstep.app.ui.components.dialog.GradeEditDialog
import com.nextstep.app.ui.components.dialog.TaskEditDialog
import com.nextstep.app.ui.components.dialog.TextInputDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * 기록하기 시트. 모든 탭의 + 버튼이 여는 유일한 쓰기 입구입니다. 항목을 고르면 대화상자 하나로 끝나고, 저장 후 한 줄 토스트가 뜹니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(caps: Capabilities, studentLevel: StudentUiLevel?, onDismiss: () -> Unit, onOpenTimer: () -> Unit, viewModel: QuickAddViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var picked by remember { mutableStateOf<QuickAddAction?>(null) }
    val context = LocalContext.current

    LaunchedEffect(state.savedMessage) {
        state.savedMessage?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.onEvent(QuickAddEvent.ClearMessage)
            onDismiss()
        }
    }

    if (studentLevel?.kid?.pictureRecord == true) {
        // 아이 모드: 입력 양식 없이 그림 타일 한 번으로 기록
        ModalBottomSheet(onDismissRequest = onDismiss) {
            KidRecordGrid(
                onRecord = { viewModel.onEvent(QuickAddEvent.KidRecordTap(it)) },
                onTimer = if (QuickAddAction.TIMER in QuickAddAction.availableFor(caps, studentLevel)) ({ onDismiss(); onOpenTimer() }) else null,
            )
        }
        return
    }

    if (picked == null) {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 24.dp)) {
                Text("무엇을 남길까요?", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                QuickAddAction.availableFor(caps, studentLevel).forEach { action ->
                    QuickAddItem(action) { if (action == QuickAddAction.TIMER) { onDismiss(); onOpenTimer() } else picked = action }
                }
            }
        }
    }

    when (picked) {
        QuickAddAction.CHEER -> TextInputDialog(title = "격려 한마디", label = "예: 오늘 25분 스스로 끝냈네!", onConfirm = { viewModel.onEvent(QuickAddEvent.Cheer(it)) }, onDismiss = onDismiss)
        QuickAddAction.ACTIVITY -> ActivityEditDialog(existing = null, today = state.today, onConfirm = { viewModel.onEvent(QuickAddEvent.SaveActivity(it)) }, onDismiss = onDismiss)
        QuickAddAction.TASK -> TaskEditDialog(existing = null, subjects = state.subjects, defaultDate = state.today, onDismiss = onDismiss) { title, subjectId, type, due ->
            viewModel.onEvent(QuickAddEvent.SaveTask(title, subjectId, type, due, caps.actingRoleName))
        }
        QuickAddAction.GRADE -> GradeEditDialog(existing = null, subjects = state.subjects, onDismiss = onDismiss, onDelete = null) { subjectId, title, examType, score, max, avg, date, memo ->
            viewModel.onEvent(QuickAddEvent.SaveGrade(subjectId, title, examType, score, max, avg, date, memo))
        }
        QuickAddAction.EVENT -> EventEditDialog(existing = null, subjects = state.subjects, defaultDate = state.today, onDismiss = onDismiss, onDelete = null) { title, subjectId, type, date, start, end, repeat, location, memo ->
            viewModel.onEvent(QuickAddEvent.SaveEvent(title, subjectId, type, date, start, end, repeat, location, memo))
        }
        QuickAddAction.TIMER, null -> Unit
    }
}

@Composable
private fun QuickAddItem(action: QuickAddAction, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(iconFor(action), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Spacer(Modifier.size(14.dp))
        Column(Modifier.weight(1f)) {
            Text(action.title, style = MaterialTheme.typography.titleMedium)
            Text(action.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    Spacer(Modifier.height(2.dp))
}

private fun iconFor(action: QuickAddAction): ImageVector = when (action) {
    QuickAddAction.CHEER -> Icons.Default.Favorite
    QuickAddAction.ACTIVITY -> Icons.AutoMirrored.Filled.MenuBook
    QuickAddAction.TASK -> Icons.Default.CheckCircle
    QuickAddAction.GRADE -> Icons.Default.BarChart
    QuickAddAction.EVENT -> Icons.Default.CalendarMonth
    QuickAddAction.TIMER -> Icons.Default.Timer
}
