package com.nextstep.app.domain.access

import kotlin.random.Random

/**
 * 어른 확인: 아이 모드에서 설정·연결 해제 같은 어른용 화면을 열기 전에 묻는 곱셈 하나(두 자리 × 한 자리).
 * 보안이 아니라 저학년 아이가 실수로 들어가지 않게 하는 문턱입니다.
 */
data class ParentGate(val a: Int, val b: Int) {
    val question: String get() = "$a × $b = ?"

    fun accepts(answer: String): Boolean = answer.trim().toIntOrNull() == a * b

    companion object {
        private val FIRST = 12..19
        private val SECOND = 6..9

        fun next(random: Random = Random.Default): ParentGate = ParentGate(random.nextInt(FIRST.first, FIRST.last + 1), random.nextInt(SECOND.first, SECOND.last + 1))
    }
}
