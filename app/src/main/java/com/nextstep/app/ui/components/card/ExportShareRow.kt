package com.nextstep.app.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.export.ExportDoc
import com.nextstep.app.ui.common.shareExportPdf
import com.nextstep.app.ui.common.shareText

/** 문서 보내기: 글로(카톡·문자) · PDF 로([pdf] 가 true 일 때 — 요금제 기능 PDF_EXPORT). [fileName] 은 PDF 파일 이름. */
@Composable
fun ExportShareRow(doc: ExportDoc, fileName: String, pdf: Boolean) {
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { context.shareText(doc.text(), doc.title) }, modifier = Modifier.weight(1f)) { Text("글로 보내기") }
        if (pdf) Button(onClick = { context.shareExportPdf(doc, fileName) }, modifier = Modifier.weight(1f)) { Text("PDF로 보내기") }
    }
}
