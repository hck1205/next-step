package com.nextstep.app.ui.components.layout

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.ui.components.icon.concernIcon

/**
 * 오늘 화면 위에 붙어 있는 관심사 칩 줄: "전체"와 카드가 있는 관심사(카드 수와 함께). 기록 탭과 같은 아이콘을 씁니다.
 * 넘치면 옆으로 밀어 봅니다.
 */
@Composable
fun ConcernFilterRow(concerns: List<Pair<Concern, Int>>, selected: Concern?, onSelect: (Concern?) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = selected == null, onClick = { onSelect(null) }, label = { Text("전체") },
                leadingIcon = { Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(18.dp)) },
            )
            concerns.forEach { (concern, count) ->
                FilterChip(
                    selected = selected == concern, onClick = { onSelect(concern) }, label = { Text("${concern.label} $count") },
                    leadingIcon = { Icon(concernIcon(concern), contentDescription = null, modifier = Modifier.size(18.dp)) },
                )
            }
        }
    }
}
