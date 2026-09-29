package com.nextstep.app.ui.mentor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.report.LessonReport
import com.nextstep.app.domain.report.LessonReports
import com.nextstep.app.ui.common.shareText
import com.nextstep.app.ui.components.card.AppCard

/**
 * 수업 리포트 보내기: 담당 과목의 이번 주 공부 · 진도 · 과제 · 살펴본 것을 미리 보여 주고, 선생님 한마디를 붙여 카톡·문자로 보냅니다.
 * [compact] 면 덩어리 머리만 한 줄씩.
 */
@Composable
internal fun LessonReportCard(report: LessonReport, compact: Boolean) {
    val context = LocalContext.current
    var note by rememberSaveable { mutableStateOf("") }
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(report.title, style = MaterialTheme.typography.titleSmall)
            report.sections.forEach { s ->
                Text(if (compact) "${s.label} · ${s.lines.first()}" else "${s.label}\n" + s.lines.joinToString("\n") { "· $it" }, style = MaterialTheme.typography.bodySmall)
            }
            if (!compact) {
                OutlinedTextField(note, { note = it }, label = { Text("선생님 한마디 (선택)") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            }
            Button(onClick = { context.shareText(LessonReports.text(report, note), "수업 리포트 보내기") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Text("  카톡·문자로 보내기")
            }
        }
    }
}
