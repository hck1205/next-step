package com.nextstep.app.ui.home.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.layout.AppBarMenu
import com.nextstep.app.ui.components.layout.AppBarMenuItem
import com.nextstep.app.ui.components.layout.CompactTopBar
import com.nextstep.app.ui.home.HomeUiState

/**
 * 오늘 화면 머리 한 줄: "오늘" 옆에 날짜와 학년(예: 4월 14일 화 · 초5). 올해 문장은 올해 탭과 '올해의 공부' 카드가,
 * 오늘 공부한 시간·남은 할 일은 타이머 카드와 칩이 보여 주므로 머리에 다시 적지 않습니다. 오른쪽 ⋮ 에는 바로가기([menu]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeTopBar(state: HomeUiState, menu: List<AppBarMenuItem>, scrollBehavior: TopAppBarScrollBehavior) {
    CompactTopBar(
        title = "오늘",
        caption = listOfNotNull(DateUtils.formatDay(state.today), state.year?.label).joinToString(" · "),
        scrollBehavior = scrollBehavior,
        actions = { AppBarMenu(menu) },
    )
}
