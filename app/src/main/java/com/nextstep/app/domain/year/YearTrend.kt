package com.nextstep.app.domain.year

/**
 * 그 나이의 한국 교육열 한 장: [common] 은 주변에서 흔히 하는 것(조사 숫자 포함), [advice] 는 이 앱이 권하는 것.
 * 부모가 "다들 하는데 우리만 안 하나" 불안할 때 보는 카드라 비교가 아니라 기준을 줍니다.
 */
data class YearTrend(val common: String, val advice: String)
