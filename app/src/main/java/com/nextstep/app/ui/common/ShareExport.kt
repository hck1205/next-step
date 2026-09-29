package com.nextstep.app.ui.common

import android.content.Context
import com.nextstep.app.domain.export.ExportDoc

/** 문서를 PDF 로 만들어 보냅니다. 파일 이름은 [fileName](확장자 없이). */
fun Context.shareExportPdf(doc: ExportDoc, fileName: String) {
    shareFile(PdfExporter(this).write(doc, fileName), "application/pdf", doc.title)
}
