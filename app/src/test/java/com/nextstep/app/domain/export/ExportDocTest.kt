package com.nextstep.app.domain.export

import com.nextstep.app.domain.report.ReportSection
import org.junit.Assert.assertEquals
import org.junit.Test

class ExportDocTest {
    @Test
    fun textLaysOutTitleSectionsAndFooter() {
        val doc = ExportDoc("지우의 3월", "2029년 3월", listOf(ReportSection("공부", listOf("12시간", "18일"))))
        assertEquals("지우의 3월\n2029년 3월\n\n■ 공부\n  · 12시간\n  · 18일\n\n— NextStep", doc.text())
        assertEquals("제목\n\n— x", ExportDoc("제목", "", emptyList(), footer = "x").text())
    }
}
