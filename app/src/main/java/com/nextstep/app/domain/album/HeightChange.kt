package com.nextstep.app.domain.album

/** 학년도 안의 첫 키와 마지막 키(cm). */
data class HeightChange(val fromCm: Double, val toCm: Double) {
    val gainCm: Double get() = toCm - fromCm
}
