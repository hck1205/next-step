package com.nextstep.app.domain.gamify

/**
 * 레벨: [number] 번째 칸, 이 칸이 시작되는 경험치 [from] 과 다음 칸 [to]. 칸은 갈수록 조금씩 넓어집니다(10 · 20 · 30 …).
 */
data class GameLevel(val number: Int, val title: String, val from: Int, val to: Int) {
    fun progress(xp: Int): Float = ((xp - from).toFloat() / (to - from)).coerceIn(0f, 1f)
    fun remaining(xp: Int): Int = (to - xp).coerceAtLeast(0)

    companion object {
        private val TITLES = listOf("첫걸음", "꾸준이", "도전자", "탐험가", "개척자", "실력자", "달인", "고수", "스승", "전설")
        private const val STEP = 10

        /** [n] 레벨이 시작되는 경험치: 10 × (n-1) × n / 2 → 0, 10, 30, 60, 100, 150 … */
        fun threshold(n: Int): Int = STEP * (n - 1) * n / 2

        fun of(xp: Int): GameLevel {
            var n = 1
            while (threshold(n + 1) <= xp) n++
            return level(n)
        }

        fun level(n: Int): GameLevel {
            val title = TITLES.getOrNull(n - 1) ?: "${TITLES.last()} +${n - TITLES.size}"
            return GameLevel(n, title, threshold(n), threshold(n + 1))
        }
    }
}
