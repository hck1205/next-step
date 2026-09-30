package com.nextstep.app.ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.lesson.LessonPlan
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.input.ChipRow
import com.nextstep.app.ui.components.input.TimeField
import java.time.DayOfWeek

/** 멘토의 수업 일정 정하기: 요일 · 시작·끝 시각 · 한 달 수업료 · 받는 날(비우면 수업료 알림 없음). */
@Composable
fun LessonPlanDialog(initial: LessonPlan, onDismiss: () -> Unit, onSave: (LessonPlan) -> Unit) {
    var days by remember { mutableStateOf(initial.days) }
    var start by remember { mutableStateOf(initial.startMinute.takeIf { initial.isSet } ?: DEFAULT_START) }
    var end by remember { mutableStateOf(initial.endMinute.takeIf { initial.isSet } ?: (DEFAULT_START + DEFAULT_LENGTH)) }
    var fee by remember { mutableStateOf(initial.fee.takeIf { it > 0 }?.toString().orEmpty()) }
    var feeDay by remember { mutableStateOf(initial.feeDay.takeIf { it > 0 }?.toString().orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("수업 일정 정하기") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipRow(DayOfWeek.entries, { it in days }, { DateUtils.dayOfWeekLabel(it) }, { d -> days = if (d in days) days - d else days + d }, title = "요일")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeField("시작", DateUtils.timeOfMinute(start), { start = DateUtils.minuteOf(it) }, Modifier.weight(1f))
                    TimeField("끝", DateUtils.timeOfMinute(end), { end = DateUtils.minuteOf(it) }, Modifier.weight(1f))
                }
                OutlinedTextField(fee, { fee = it.filter(Char::isDigit) }, label = { Text("한 달 수업료(원)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(feeDay, { feeDay = it.filter(Char::isDigit).take(2) }, label = { Text("받는 날(1~31일)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(LessonPlan(days, start, maxOf(start, end), fee.toIntOrNull() ?: 0, feeDay.toIntOrNull()?.coerceIn(0, MAX_DAY) ?: 0)); onDismiss()
            }) { Text("저장") }
        },
        dismissButton = { CancelButton(onDismiss) },
    )
}

private const val DEFAULT_START = 16 * DateUtils.MINUTES_IN_HOUR
private const val DEFAULT_LENGTH = 90
private const val MAX_DAY = 31
