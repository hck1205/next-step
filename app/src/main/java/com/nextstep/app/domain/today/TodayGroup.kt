package com.nextstep.app.domain.today

import com.nextstep.app.domain.hub.Concern

/** 오늘 화면의 한 묶음: 관심사 하나와 거기 속한 카드들(화면 순서). */
data class TodayGroup<T>(val concern: Concern, val cards: List<T>)
