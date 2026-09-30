package com.nextstep.app.domain.today

import com.nextstep.app.domain.hub.Concern

/**
 * 오늘 화면의 틀: 카드를 관심사(기록 탭과 같은 [Concern])로 묶습니다. 학생·학부모·멘토 오늘 화면이 모두 씁니다.
 * - "전체"는 먼저 볼 카드 한 장([focus])만 펼치고, 나머지는 관심사마다 한 줄([rest]: 이름 · 카드 이름 · 개수)로 접습니다.
 *   한 줄을 누르면 그 관심사의 카드만 크게. 처음 보이는 양을 줄여 쉽게 하되, 카드는 하나도 빼지 않습니다.
 * - 묶음 순서는 카드가 처음 나온 순서(학생은 올해 프로필이 앞에 둔 카드의 관심사가 먼저), 묶음 안은 카드 순서 그대로.
 */
object TodayLayout {
    fun <T> group(cards: List<T>, concernOf: (T) -> Concern): List<TodayGroup<T>> =
        cards.groupBy(concernOf).map { (concern, list) -> TodayGroup(concern, list) }

    /** "전체"에서 먼저 크게 보여 줄 카드: [preferred] 순서대로 지금 있는 첫 카드, 없으면 화면 첫 카드. */
    fun <T> focus(cards: List<T>, preferred: List<T>): T? = preferred.firstOrNull { it in cards } ?: cards.firstOrNull()

    /** 먼저 볼 카드를 뺀 나머지 묶음(비면 뺌). "더 보기"에 관심사마다 한 줄로 보입니다. */
    fun <T> rest(groups: List<TodayGroup<T>>, focus: T?): List<TodayGroup<T>> =
        groups.map { g -> g.copy(cards = g.cards.filter { it != focus }) }.filter { it.cards.isNotEmpty() }

    /** 고른 관심사의 카드. null 이면 전부(화면 순서). 없는 관심사면 빈 목록. */
    fun <T> cardsOf(groups: List<TodayGroup<T>>, concern: Concern?): List<T> =
        if (concern == null) groups.flatMap { it.cards } else groups.firstOrNull { it.concern == concern }?.cards.orEmpty()
}
