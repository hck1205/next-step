package com.nextstep.app.ui.common

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.nextstep.app.domain.export.ExportDoc
import java.io.File

/**
 * [ExportDoc] 을 A4 PDF 로 씁니다(앱 캐시 exports/ 안). 글자만 그리는 단순한 문서라 어떤 기기에서도 같게 열립니다.
 * 줄이 넘치면 글자 폭으로 나누고, 쪽이 넘치면 다음 쪽으로 넘깁니다.
 */
class PdfExporter(private val context: Context) {

    fun write(doc: ExportDoc, fileName: String): File {
        val pdf = PdfDocument()
        val pen = Pen(pdf)
        pen.line(doc.title, TITLE_SIZE, bold = true)
        if (doc.subtitle.isNotBlank()) pen.line(doc.subtitle, BODY_SIZE)
        doc.sections.forEach { s ->
            pen.gap()
            pen.line("■ ${s.label}", HEAD_SIZE, bold = true)
            s.lines.forEach { pen.line("· $it", BODY_SIZE, indent = INDENT) }
        }
        pen.gap()
        pen.line("— ${doc.footer}", SMALL_SIZE)
        pen.finish()
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "$fileName.pdf")
        file.outputStream().use { pdf.writeTo(it) }
        pdf.close()
        return file
    }

    /** 쪽을 넘기며 한 줄씩 그리는 붓. */
    private class Pen(private val pdf: PdfDocument) {
        private var number = 0
        private var page: PdfDocument.Page = newPage()
        private var y = MARGIN.toFloat()

        private fun newPage(): PdfDocument.Page = pdf.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, ++number).create())

        fun gap() { y += GAP }

        fun line(text: String, size: Float, bold: Boolean = false, indent: Int = 0) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = size; typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT }
            wrap(text, paint, (PAGE_W - 2 * MARGIN - indent).toFloat()).forEach { part ->
                if (y + size > PAGE_H - MARGIN) { pdf.finishPage(page); page = newPage(); y = MARGIN.toFloat() }
                y += size
                page.canvas.drawText(part, (MARGIN + indent).toFloat(), y, paint)
                y += size * LINE_GAP
            }
        }

        fun finish() = pdf.finishPage(page)

        private fun wrap(text: String, paint: Paint, width: Float): List<String> {
            val out = mutableListOf<String>()
            var rest = text
            while (rest.isNotEmpty()) {
                val n = paint.breakText(rest, true, width, null).coerceAtLeast(1)
                out += rest.take(n)
                rest = rest.drop(n)
            }
            return out.ifEmpty { listOf("") }
        }
    }

    private companion object {
        const val PAGE_W = 595
        const val PAGE_H = 842
        const val MARGIN = 48
        const val INDENT = 14
        const val GAP = 10f
        const val LINE_GAP = 0.45f
        const val TITLE_SIZE = 20f
        const val HEAD_SIZE = 14f
        const val BODY_SIZE = 12f
        const val SMALL_SIZE = 10f
    }
}
