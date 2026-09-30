package com.nextstep.app.ui.common

import android.content.Context
import com.nextstep.app.domain.export.ExportDoc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 문서를 PDF 로 만들어 보냅니다. 파일 이름은 [fileName](확장자 없이). 파일 쓰기는 IO 스레드에서 합니다. */
suspend fun Context.shareExportPdf(doc: ExportDoc, fileName: String) {
    val file = withContext(Dispatchers.IO) { PdfExporter(this@shareExportPdf).write(doc, fileName) }
    shareFile(file, "application/pdf", doc.title)
}
