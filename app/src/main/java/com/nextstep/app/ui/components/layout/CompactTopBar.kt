package com.nextstep.app.ui.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * 탭 첫 화면의 얇은 상단 바(높이 [COMPACT_BAR_DP]dp). 날짜·학년 같은 설명은 줄을 늘리지 않고 제목 옆에 작은 글씨([caption])로,
 * 동기화 같은 작은 표식은 그 뒤([badge])에 붙입니다. [scrollBehavior] 를 주면 목록을 올릴 때 바 색이 바뀌어 본문과 나뉩니다
 * (화면 Scaffold 에 `Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactTopBar(
    title: String,
    caption: String? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    badge: (@Composable () -> Unit)? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.alignByBaseline().weight(1f, fill = false))
                caption?.let {
                    Text(
                        it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.alignByBaseline().weight(1f, fill = false),
                    )
                }
                badge?.invoke()
            }
        },
        navigationIcon = navigationIcon,
        actions = actions,
        expandedHeight = COMPACT_BAR_DP.dp,
        scrollBehavior = scrollBehavior,
    )
}

/** 기본 64dp 보다 한 단 얇게. 글자 크기를 키워도 제목 한 줄은 들어갑니다. */
private const val COMPACT_BAR_DP = 56
