package com.nextstep.app.ui.components.layout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

/**
 * 옆으로 넘기는 카드 슬라이드. 다음 카드가 살짝 보여 넘길 수 있다는 걸 알리고, 아래 점으로 몇 번째인지 보여 줍니다.
 * 한 관심사의 카드를 한 줄 높이에 담아 오늘 화면이 짧아집니다. 폭이 넉넉하면(폴더블·태블릿·가로) 한 번에 두 장씩 보입니다.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardCarousel(count: Int, page: @Composable (Int) -> Unit) {
    val state = rememberPagerState { count }
    BoxWithConstraints {
        val perView = if (maxWidth >= TWO_UP_FROM.dp && count > 1) 2 else 1
        // 다 보이면 다음 장 엿보기 여백이 필요 없습니다.
        val peek = if (count > perView) PEEK else 0
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            HorizontalPager(
                state = state, contentPadding = PaddingValues(end = peek.dp), pageSpacing = 10.dp, pageSize = PerView(perView),
                verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth(),
            ) { i -> page(i) }
            if (count > perView) {
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
}

/** 한 화면에 [count] 장: 오른쪽 살짝 보이는 다음 장을 뺀 폭을 똑같이 나눕니다. */
private class PerView(private val count: Int) : PageSize {
    override fun Density.calculateMainAxisPageSize(availableSpace: Int, pageSpacing: Int): Int =
        (availableSpace - (count - 1) * pageSpacing) / count
}

private const val PEEK = 36
/** 이 폭(dp) 이상이면 두 장씩. 폰 세로(360~430dp)는 한 장. */
private const val TWO_UP_FROM = 560
