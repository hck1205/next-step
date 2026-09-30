package com.nextstep.app.domain.report

/** 수업 리포트의 한 덩어리: 머리([label])와 그 아래 줄. */
data class ReportSection(val label: String, val lines: List<String>)
