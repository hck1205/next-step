package com.nextstep.app.ui.components.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.domain.today.TodayGroup
import com.nextstep.app.domain.today.TodayLayout

/**
 * 오늘 화면의 몸통(학생·학부모·멘토 공통, 묶음은 domain/today/TodayLayout). 처음 보이는 양을 줄여 쉽게 봅니다.
 * - "전체": [lead](상태 요약·타이머) → "먼저 볼 것" 카드 한 장([preferred] 순서, TodayLayout.focus) →
 *   "더 보기" 목록에 나머지 관심사가 한 줄씩([ConcernRow]: 이름 · 안에 든 카드 이름 · 개수).
 * - 한 줄을 누르면 그 관심사 카드만 세로로 크게, 맨 위 "‹ 오늘"로 돌아갑니다. 칩 줄을 늘어놓지 않아 한 번에 한 층만 보입니다.
 * - 펼치기([onExpand])는 자세히 시트를 엽니다.
 */
fun <T> LazyListScope.todayBoard(
    groups: List<TodayGroup<T>>,
    filter: Concern?,
    onFilter: (Concern?) -> Unit,
    title: (T) -> String,
    key: (T) -> String,
    onExpand: (T) -> Unit,
    preferred: List<T> = emptyList(),
    lead: (@Composable () -> Unit)? = null,
    body: @Composable (card: T, compact: Boolean) -> Unit,
) {
    if (filter != null) {
        item(key = "back") { TitleBackRow(filter.label, filter.question, backLabel = "오늘", onBack = { onFilter(null) }) }
        TodayLayout.cardsOf(groups, filter).forEach { card ->
            item(key = "only-${key(card)}") { TodayCardFrame(title(card)) { body(card, false) } }
        }
        return
    }
    if (lead != null) item(key = "today-lead") { lead() }
    val cards = TodayLayout.cardsOf(groups, null)
    val focus = TodayLayout.focus(cards, preferred)
    val concernOf = { card: T -> groups.first { card in it.cards }.concern }
    if (focus != null) item(key = "focus-${key(focus)}") {
        Column {
            FocusHeader(concernOf(focus), subtitle = title(focus).ifBlank { null }, onExpand = { onExpand(focus) })
            body(focus, true)
        }
    }
    val rest = TodayLayout.rest(groups, focus)
    if (rest.isNotEmpty()) item(key = "more") { MoreRows(rest, title, onFilter) }
}

/** "더 보기": 먼저 볼 카드를 뺀 관심사마다 한 줄(안에 든 카드 이름 · 개수). */
@Composable
private fun <T> MoreRows(rest: List<TodayGroup<T>>, title: (T) -> String, onFilter: (Concern?) -> Unit) {
    Column {
        Text("더 보기", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
        Card {
            rest.forEachIndexed { i, g ->
                if (i > 0) HorizontalDivider()
                val summary = g.cards.map(title).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { g.concern.question }
                ConcernRow(g.concern, summary = summary, count = g.cards.size, onOpen = { onFilter(g.concern) })
            }
        }
    }
}
