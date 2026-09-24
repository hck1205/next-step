package com.nextstep.app.domain.mission

/** 목표 날짜 [daysBefore]일 전에 할 단계. 음수면 목표 날짜 뒤(예: 시험 결과 분석). */
data class MissionStepTemplate(val daysBefore: Int, val title: String, val detail: String = "")
