package com.nextstep.app.ui.components.layout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * 옆으로 넘기는 카드 슬라이드. 다음 카드가 살짝 보여 넘길 수 있다는 걸 알리고, 아래 점으로 몇 번째인지 보여 줍니다.
 * 한 관심사의 카드를 한 줄 높이에 담아 오늘 화면이 짧아집니다.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardCarousel(count: Int, page: @Composable (Int) -> Unit) {
    val state = rememberPagerState { count }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        HorizontalPager(
            state = state, contentPadding = PaddingValues(end = PEEK.dp), pageSpacing = 10.dp,
            verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth(),
        ) { i -> page(i) }
        if (count > 1) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
                repeat(count) { i ->
                    Box(
                        Modifier.size(if (i == state.currentPage) 8.dp else 6.dp).clip(CircleShape)
                            .background(if (i == state.currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    )
                }
            }
        }
    }
}

private const val PEEK = 36
