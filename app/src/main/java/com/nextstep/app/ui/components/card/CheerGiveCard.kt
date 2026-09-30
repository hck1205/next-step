package com.nextstep.app.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.cheer.CheerKind
import com.nextstep.app.domain.cheer.CheerTarget
import com.nextstep.app.ui.common.UiDefaults

/** 학부모 오늘 화면의 "해낸 일 응원하기": 최근 끝낸 일마다 응원 네 가지 중 하나. 같은 것을 다시 누르면 거둡니다. [compact] 면 두 줄까지. */
@Composable
fun CheerGiveCard(targets: List<CheerTarget>, compact: Boolean, onCheer: (CheerTarget, CheerKind) -> Unit) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            targets.take(if (compact) UiDefaults.COMPACT_ROWS else targets.size).forEach { target ->
                Column {
                    Text(target.task.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CheerKind.entries.forEach { kind ->
                            FilterChip(selected = target.given == kind, onClick = { onCheer(target, kind) }, label = { Text(kind.emoji) })
                        }
                    }
                }
            }
        }
    }
}
