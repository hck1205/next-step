package com.nextstep.app.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.CheerEntity
import com.nextstep.app.domain.cheer.Cheers
import com.nextstep.app.ui.common.UiDefaults

/** 학생 오늘 화면의 "받은 응원": 가족이 해낸 일에 붙인 응원. "고마워요"를 누르면 확인한 것으로 내려갑니다. [compact] 면 두 줄까지. */
@Composable
fun CheerReceivedCard(cheers: List<CheerEntity>, compact: Boolean, onThanks: (List<String>) -> Unit) {
    val shown = cheers.take(if (compact) UiDefaults.COMPACT_ROWS else cheers.size)
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            shown.forEach { Text(Cheers.line(it), style = MaterialTheme.typography.bodyLarge) }
            if (cheers.size > shown.size) Text("응원이 ${cheers.size - shown.size}개 더 있어요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilledTonalButton(onClick = { onThanks(shown.map { it.id }) }) { Text("고마워요 💛") }
        }
    }
}
