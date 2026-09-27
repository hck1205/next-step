package com.nextstep.app.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.home.HomeUiState

/**
 * 오늘 화면 머리: "오늘" · 올해 한 줄(해마다 바뀜, 예: 초3 · 사회·과학·영어가 새로 시작되는 해) · 날짜.
 * 숫자를 보는 나이는 날짜 뒤에 남은 할 일 수와 오늘 공부한 시간을 붙입니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeTopBar(state: HomeUiState) {
    TopAppBar(
        title = {
            Column {
                Text("오늘", style = MaterialTheme.typography.titleLarge)
                state.year?.let { y -> Text("${y.label} · ${y.theme}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                Text(
                    if (!state.level.showsNumbers) DateUtils.formatFullDate(state.today)
                    else "${DateUtils.formatFullDate(state.today)} · 할 것 ${state.pendingTasks.size}개" + if (state.todayMinutes > 0) " · 오늘 ${DateUtils.formatMinutes(state.todayMinutes)}" else "",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
