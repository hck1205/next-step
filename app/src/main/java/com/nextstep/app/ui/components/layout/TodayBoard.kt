package com.nextstep.app.ui.components.layout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.domain.today.TodayGroup
import com.nextstep.app.domain.today.TodayLayout

/**
 * 오늘 화면의 몸통(학생·학부모·멘토 공통, 묶음은 domain/today/TodayLayout).
 * - 관심사 칩 줄이 위에 붙어 있습니다(묶음이 둘 이상일 때).
 * - "전체": 관심사마다 머리 한 줄 + 카드 슬라이드(카드가 하나면 슬라이드 없이). 카드는 모두 줄인 모양([body] 의 compact = true)이고,
 *   펼치기 버튼이 [onExpand] 로 자세히 시트를 엽니다.
 * - 칩을 고르면 그 관심사 카드만 세로로 크게.
 */
@OptIn(ExperimentalFoundationApi::class)
fun <T> LazyListScope.todayBoard(
    groups: List<TodayGroup<T>>,
    filter: Concern?,
    onFilter: (Concern?) -> Unit,
    title: (T) -> String,
    key: (T) -> String,
    onExpand: (T) -> Unit,
    body: @Composable (card: T, compact: Boolean) -> Unit,
) {
    if (groups.size > 1) stickyHeader(key = "concern-filter") { ConcernFilterRow(groups.map { it.concern to it.cards.size }, filter, onFilter) }
    if (filter == null) {
        groups.forEach { g ->
            val only = g.cards.singleOrNull()
            if (only != null) {
                // 카드가 하나면 머리 한 줄에 카드 이름·펼치기까지(제목 줄이 두 번 쌓이지 않게).
                item(key = "card-${key(only)}") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        GroupHeader(g.concern, 1, onSeeAll = null, subtitle = title(only).ifBlank { null }, onExpand = { onExpand(only) })
                        body(only, true)
                    }
                }
            } else {
                item(key = "group-${g.concern.name}") { GroupHeader(g.concern, g.cards.size, onSeeAll = { onFilter(g.concern) }) }
                item(key = "slide-${g.concern.name}") {
                    CardCarousel(g.cards.size) { i ->
                        val card = g.cards[i]
                        TodayCardFrame(title(card), onExpand = { onExpand(card) }) { body(card, true) }
                    }
                }
            }
        }
    } else {
        TodayLayout.cardsOf(groups, filter).forEach { card ->
            item(key = "only-${key(card)}") { TodayCardFrame(title(card), onExpand = null) { body(card, false) } }
        }
    }
}
