package com.nextstep.app.domain.year

/**
 * 앞서 가기([YearTier.AHEAD]): 그해 기본을 채운 뒤 여유가 있을 때, 미리 해 두면 다음 해의 고비를 쉽게 넘는 일.
 * 키는 YearProfiles 와 같습니다. 줄마다 도착점([YearTask.bar])과 까닭([YearTask.why])이 있습니다.
 *
 * 고르는 원칙
 * - 빨리보다 깊게·넓게: 다음 학년 진도를 당기기보다 사고력 문제·서술형 풀이·어휘·배경지식·독서처럼 모든 과목에 남는 힘을 먼저.
 * - 다음 해 내용은 학년 말(주로 겨울방학)에 개념만 가볍게, 주 2~4회를 넘지 않게.
 * - 스스로 하는 도구(계획·요약·오답·복기)를 한 해 먼저 손에 익히기: 자기주도 사다리의 다음 칸과 맞물립니다.
 * - 학령 전(만 0~6세)은 공부가 아니라 대화·놀이·호기심을 넓히는 것만 둡니다(학습지·문제집·학원 없음, 과열 가드).
 *   만 0~1세는 부모가 하는 일만.
 * - 수면 권장 시간(초 9~12시간, 중·고 8~10시간)을 줄이는 일은 넣지 않습니다.
 */
object AheadPlans {
    fun forYear(key: String): List<YearTask> = plans[key].orEmpty()

    /** 화면의 묶음 이름: 학령 전은 공부가 아니므로 "더 해 보면 좋은 것", 학교부터 "앞서 가기". */
    fun heading(key: String): String = if (isPreschool(key)) "더 해 보면 좋은 것" else YearTier.AHEAD.label

    /** 묶음 아래 한 줄 안내. */
    fun note(key: String): String =
        if (isPreschool(key)) "공부가 아니라 놀이와 대화로, 아이가 즐거워할 때만 해요"
        else "기본을 채운 뒤 여유가 있을 때만. 못 해도 밀리거나 알림이 오지 않아요"

    private fun isPreschool(key: String): Boolean = key.startsWith(PRESCHOOL_PREFIX)
    private const val PRESCHOOL_PREFIX = "a"

    /** 학령 전 · 학교부터 표를 한 번 합쳐 둡니다. */
    private val plans: Map<String, List<YearTask>> = PreschoolAhead.byYear + SchoolAhead.byYear
}
