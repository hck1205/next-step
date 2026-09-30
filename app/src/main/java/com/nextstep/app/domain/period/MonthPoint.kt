package com.nextstep.app.domain.period

/** 긴 흐름 차트의 한 달: 이름("3월") · 공부 시간(분) · 마감 할 일을 끝낸 비율(없으면 null). */
data class MonthPoint(val label: String, val minutes: Int, val doneRate: Int?)
