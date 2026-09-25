package com.nextstep.app.domain.hub

/**
 * "한눈에"의 관심사 타일 한 장: 큰 한 줄([headline])과 작은 한 줄([detail]).
 * [attention] 이면 타일에 주황 점을 찍어 먼저 볼 곳을 알려 줍니다.
 */
data class ConcernDigest(
    val concern: Concern,
    val headline: String,
    val detail: String? = null,
    val attention: Boolean = false,
)
