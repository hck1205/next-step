package com.nextstep.app.domain.health

/** 성장 기록에서 나온 참고 신호. 진단이 아니라 "검진에서 물어볼 것"입니다. */
data class GrowthSignal(val title: String, val detail: String, val level: GrowthSignalLevel)
