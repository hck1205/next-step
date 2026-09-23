package com.nextstep.app.domain.cheer

import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.time.DateUtils

/**
 * 오늘 한 일에 맞춘 칭찬 문구. 근거가 있는 문구를 먼저, 없으면 격려 한 줄, 마지막엔 항상 "언제든 얘기해".
 * 어린 자녀는 과정을, 고등학생은 판단을 칭찬합니다.
 */
object CheerSuggestions {
    private const val PRAISE_MINUTES = 60
    private const val PRAISE_STREAK_DAYS = 3

    fun build(s: CheerSnapshot, stage: GrowthStage?): List<String> = buildList {
        if (s.todayMinutes >= PRAISE_MINUTES) add("오늘 ${DateUtils.formatMinutes(s.todayMinutes)} 공부한 거 봤어. 정말 대단해!")
        if (s.streak >= PRAISE_STREAK_DAYS) add("${s.streak}일 연속으로 공부했네. 꾸준함이 최고야 👏")
        if (s.doneToday > 0) add(
            when (stage) {
                GrowthStage.EARLY_ELEMENTARY, GrowthStage.UPPER_ELEMENTARY -> "오늘 할 일 ${s.doneToday}개 끝까지 해냈네. 스스로 한 게 제일 멋져!"
                GrowthStage.HIGH -> "계획한 ${s.doneToday}개를 네 판단대로 끝냈구나. 믿고 있어."
                else -> "오늘 할 일 ${s.doneToday}개 끝낸 거 멋지다!"
            },
        )
        if (s.reviewedToday > 0) add("복습까지 챙기다니, 배운 걸 내 것으로 만들고 있구나.")
        if (s.topSubjectName != null && s.todayMinutes > 0) add("${s.topSubjectName} 열심히 하는 모습 보기 좋아.")
        if (isEmpty()) add("오늘도 수고했어. 네 속도대로 가면 돼 🙂")
        add("힘들면 언제든 얘기해. 응원하고 있어.")
    }
}
