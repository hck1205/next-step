package com.nextstep.app.domain.text

/**
 * 이름 뒤에 붙는 조사를 받침에 맞춥니다: 수학을·영어를, 수학은·영어는, 수학이·영어가.
 * 마지막 글자가 한글이 아니면 받침 없는 쪽으로 붙입니다.
 */
object Josa {
    fun withObject(word: String): String = word + if (hasFinal(word)) "을" else "를"
    fun withTopic(word: String): String = word + if (hasFinal(word)) "은" else "는"
    fun withSubject(word: String): String = word + if (hasFinal(word)) "이" else "가"

    private fun hasFinal(word: String): Boolean {
        val last = word.lastOrNull() ?: return false
        if (last !in HANGUL_FIRST..HANGUL_LAST) return false
        return (last - HANGUL_FIRST) % FINALS != 0
    }

    private const val HANGUL_FIRST = '가'
    private const val HANGUL_LAST = '힣'
    private const val FINALS = 28
}
