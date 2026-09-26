package com.nextstep.app.ui.components.icon

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 채운 별 [count] 개(별점·강점 신호). 글자 "★" 대신 아이콘으로 그려 글꼴마다 모양이 달라지지 않게 합니다. */
@Composable
fun StarRow(count: Int, tint: Color, size: Dp = 14.dp) {
    Row(Modifier.semantics { contentDescription = "별 ${count}개" }) {
        repeat(count) { Icon(Icons.Default.Star, contentDescription = null, tint = tint, modifier = Modifier.size(size)) }
    }
}
