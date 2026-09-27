package com.nextstep.app.domain.today

import com.nextstep.app.domain.hub.Concern

/**
 * 오늘 화면의 틀: 카드를 관심사(기록 탭과 같은 [Concern])로 묶습니다. 학생·학부모·멘토 오늘 화면이 모두 씁니다.
 * - "전체"에서는 묶음마다 한 줄(카드가 둘 이상이면 옆으로 넘기는 슬라이드), 관심사 칩을 고르면 그 묶음만 크게.
 * - 묶음 순서는 카드가 처음 나온 순서(학생은 올해 프로필이 앞에 둔 카드의 관심사가 먼저), 묶음 안은 카드 순서 그대로.
 */
object TodayLayout {
    fun <T> group(cards: List<T>, concernOf: (T) -> Concern): List<TodayGroup<T>> =
        cards.groupBy(concernOf).map { (concern, list) -> TodayGroup(concern, list) }

    /** 고른 관심사의 카드. null 이면 전부(화면 순서). 없는 관심사면 빈 목록. */
    fun <T> cardsOf(groups: List<TodayGroup<T>>, concern: Concern?): List<T> =
        if (concern == null) groups.flatMap { it.cards } else groups.firstOrNull { it.concern == concern }?.cards.orEmpty()
}
