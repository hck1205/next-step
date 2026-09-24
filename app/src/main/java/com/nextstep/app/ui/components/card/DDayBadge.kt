package com.nextstep.app.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** D-day 알약. 7일 이내는 강조색, 지났으면 채운 경고색. (UX 가이드 4-1) */
@Composable
fun DDayBadge(daysLeft: Int, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val urgent = daysLeft in 0..URGENT_DAYS
    val bg = if (daysLeft < 0) scheme.error else if (urgent) scheme.tertiaryContainer else scheme.surfaceVariant
    val fg = if (daysLeft < 0) scheme.onError else if (urgent) scheme.onTertiaryContainer else scheme.onSurfaceVariant
    Text(
        text = when { daysLeft == 0 -> "D-day"; daysLeft > 0 -> "D-$daysLeft"; else -> "D+${-daysLeft}" },
        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = fg,
        modifier = modifier.background(bg, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

/** 7일 이내면 서둘러야 할 마감으로 봅니다. */
const val URGENT_DAYS = 7
