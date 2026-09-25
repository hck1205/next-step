package com.nextstep.app.ui.hub.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.ui.components.icon.concernIcon

/** 관심사 줄: 아이콘 + 이름 탭을 가로로 넘깁니다. 아래 화면을 옆으로 밀어도 같이 움직입니다. */
@Composable
internal fun ConcernTabs(concerns: List<Concern>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    ScrollableTabRow(selectedTabIndex = selected, edgePadding = 12.dp, modifier = modifier) {
        concerns.forEachIndexed { index, concern ->
            Tab(
                selected = index == selected,
                onClick = { onSelect(index) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(concernIcon(concern), contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(concern.label)
                    }
                },
            )
        }
    }
}
