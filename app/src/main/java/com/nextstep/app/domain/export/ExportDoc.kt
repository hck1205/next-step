package com.nextstep.app.domain.export

import com.nextstep.app.domain.report.ReportSection

/** PDF·글로 내보낼 문서 한 편: 제목 · 부제 · 덩어리들 · 끝맺음. 리포트·앨범이 같은 모양으로 내보냅니다. */
data class ExportDoc(val title: String, val subtitle: String, val sections: List<ReportSection>, val footer: String = "NextStep") {
    /** 카톡·문자로 보낼 글. */
    fun text(): String = buildString {
        appendLine(title)
        if (subtitle.isNotBlank()) appendLine(subtitle)
        sections.forEach { s ->
            appendLine()
            appendLine("■ ${s.label}")
            s.lines.forEach { appendLine("  · $it") }
        }
        appendLine()
        append("— $footer")
    }
}
