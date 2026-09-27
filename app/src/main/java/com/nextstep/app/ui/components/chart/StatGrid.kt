package com.nextstep.app.ui.components.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 지표 타일 줄: 폰은 두 칸씩, 넓은 화면(560dp 이상)은 한 줄에 네 칸. 한 줄의 타일은 높이를 맞춥니다. */
@Composable
fun StatGrid(tiles: List<@Composable (Modifier) -> Unit>, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier) {
        val perRow = if (maxWidth >= WIDE_DP.dp) tiles.size.coerceAtLeast(1) else 2
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tiles.chunked(perRow).forEach { row ->
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { tile -> tile(Modifier.weight(1f).fillMaxHeight()) }
                    repeat(perRow - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

private const val WIDE_DP = 560
