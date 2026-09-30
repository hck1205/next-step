package com.nextstep.app.ui.journey.components

import androidx.compose.runtime.Composable
import com.nextstep.app.ui.components.dialog.TextInputDialog
import com.nextstep.app.ui.journey.JourneyEvent
import java.time.LocalDate

/** 열린 창([dialog]) 하나를 그립니다. 저장하면 닫힙니다. */
@Composable
internal fun JourneyDialogs(dialog: JourneyDialog?, today: LocalDate, onEvent: (JourneyEvent) -> Unit, onDismiss: () -> Unit) {
    when (dialog) {
        null -> Unit
        JourneyDialog.Add -> AddMilestoneDialog(
            today = today,
            onConfirm = { title, desc, category, due, lead -> onEvent(JourneyEvent.AddCustom(title, desc, category, due, lead)); onDismiss() },
            onDismiss = onDismiss,
        )
        is JourneyDialog.Note -> TextInputDialog(
            title = "메모", label = "예: 3월에 대기 신청 완료", initial = dialog.item.note,
            onConfirm = { onEvent(JourneyEvent.SetNote(dialog.item, it)); onDismiss() }, onDismiss = onDismiss,
        )
        is JourneyDialog.DueDate -> DueDateDialog(dialog.item, onConfirm = { onEvent(JourneyEvent.SetDueDate(dialog.item, it)); onDismiss() }, onDismiss = onDismiss)
    }
}
