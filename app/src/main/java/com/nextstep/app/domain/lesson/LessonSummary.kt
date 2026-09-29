package com.nextstep.app.domain.lesson

/** 한 달 수업 요약: 정해진·기록된 수업 수와 출석·결석·보강 필요. */
data class LessonSummary(val total: Int, val done: Int, val absent: Int, val makeup: Int) {
    /** "수업 5/8회 · 결석 1 · 보강 필요 1". */
    val line: String get() = listOfNotNull("수업 $done/${total}회", absent.takeIf { it > 0 }?.let { "결석 $it" }, makeup.takeIf { it > 0 }?.let { "보강 필요 $it" }).joinToString(" · ")
}
