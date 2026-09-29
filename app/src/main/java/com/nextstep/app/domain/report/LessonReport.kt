package com.nextstep.app.domain.report

/** 학부모에게 보내는 수업 리포트 한 장: 제목 · 보낸 선생님 · 덩어리들. 보낼 글은 [LessonReports.text]. */
data class LessonReport(val title: String, val from: String, val sections: List<ReportSection>)
