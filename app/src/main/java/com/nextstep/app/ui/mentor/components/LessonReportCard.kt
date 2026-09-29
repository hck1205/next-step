package com.nextstep.app.ui.mentor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.nextstep.app.data.local.entity.ReportLogEntity
import com.nextstep.app.domain.plan.Entitlements
import com.nextstep.app.domain.plan.Feature
import com.nextstep.app.domain.report.LessonReport
import com.nextstep.app.domain.report.LessonReports
import com.nextstep.app.domain.report.ReportKind
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.shareExportPdf
import com.nextstep.app.ui.common.shareText
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.input.SegmentedRow

/**
 * 수업 리포트 보내기: 주간|월간(월간 수업 리포트 기능) → 미리 보기 → 선생님 한마디 → 카톡·문자 / PDF → 보낸 기록.
 * 서명(가족 탭 "리포트 서명")이 있으면 끝에 붙습니다. [compact] 면 덩어리 머리만 한 줄씩.
 */
@Composable
internal fun LessonReportCard(week: LessonReport, month: LessonReport?, signature: String, logs: List<ReportLogEntity>, can: Entitlements, compact: Boolean, onSent: (ReportKind, String) -> Unit) {
    var kind by rememberSaveable { mutableStateOf(ReportKind.WEEK) }
    var note by rememberSaveable { mutableStateOf("") }
    val report = if (kind == ReportKind.MONTH && month != null) month else week
    val sign = signature.takeIf { can.has(Feature.REPORT_SIGNATURE) }.orEmpty()
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!compact && month != null && can.has(Feature.MONTHLY_LESSON_REPORT)) {
                SegmentedRow(ReportKind.entries, kind, label = { it.label }, onSelect = { kind = it })
            }
            ReportPreview(report, compact)
            if (!compact) {
                OutlinedTextField(note, { note = it }, label = { Text("선생님 한마디 (선택)") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                if (sign.isNotBlank()) Text(sign, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            SendButtons(report, note, sign, pdf = can.has(Feature.PDF_EXPORT)) { onSent(kind, report.title) }
            if (!compact && can.has(Feature.REPORT_LOG) && logs.isNotEmpty()) SentLog(logs)
        }
    }
}

@Composable
private fun ReportPreview(report: LessonReport, compact: Boolean) {
    Text(report.title, style = MaterialTheme.typography.titleSmall)
    report.sections.forEach { s ->
        Text(if (compact) "${s.label} · ${s.lines.first()}" else "${s.label}\n" + s.lines.joinToString("\n") { "· $it" }, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SendButtons(report: LessonReport, note: String, signature: String, pdf: Boolean, onSent: () -> Unit) {
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { context.shareText(LessonReports.text(report, note, signature), "수업 리포트 보내기"); onSent() }, modifier = Modifier.weight(1f)) { Text("카톡·문자로") }
        if (pdf) OutlinedButton(onClick = { context.shareExportPdf(LessonReports.doc(report, note, signature), "lesson-report"); onSent() }, modifier = Modifier.weight(1f)) { Text("PDF로") }
    }
}

@Composable
private fun SentLog(logs: List<ReportLogEntity>) {
    Text("보낸 기록", style = MaterialTheme.typography.labelLarge)
    logs.forEach { log ->
        Text(
            "${DateUtils.formatShortDate(DateUtils.toLocalDate(log.sentAt))} · ${ReportKind.from(log.kind).label} · ${log.title}",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
        )
    }
}
