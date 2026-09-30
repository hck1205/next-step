package com.nextstep.app.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * 첫 화면 최상단의 상태 카드. 숫자 대신 문장으로 답하고, 작은 지표 세 개까지만 둡니다.
 * 색을 바꿔 경고하지 않습니다: 챙길 것이 있으면 문장으로 말합니다.
 */
@Composable
fun StatusCard(context: String, headline: String, tiles: List<StatusTile>, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primary).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(context, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = CONTEXT_ALPHA))
        Text(headline, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
        if (tiles.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tiles.take(MAX_TILES).forEach { tile ->
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = TILE_ALPHA)).padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(tile.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = LABEL_ALPHA))
                        Text(tile.value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

private const val MAX_TILES = 3
private const val CONTEXT_ALPHA = 0.85f
private const val TILE_ALPHA = 0.14f
private const val LABEL_ALPHA = 0.8f
