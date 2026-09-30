package com.nextstep.app.ui.growthalbum.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.album.HeightChange
import com.nextstep.app.ui.common.oneDecimal

/** "자란 것": 왼쪽에 자 눈금, 처음 키와 지금 키를 막대 둘로(높이는 같은 눈금), 위에 "+2.8cm" 배지. */
@Composable
internal fun HeightRuler(change: HeightChange) {
    val cs = MaterialTheme.colorScheme
    val low = change.fromCm - FLOOR_CM
    val span = (change.toCm - low).coerceAtLeast(1.0)
    val shape = RoundedCornerShape(18.dp)
    Box(
        Modifier.fillMaxWidth().height(200.dp).clip(shape).background(cs.surface).border(1.dp, cs.outlineVariant, shape)
            .clearAndSetSemantics { contentDescription = "키 ${change.fromCm.oneDecimal()}cm에서 ${change.toCm.oneDecimal()}cm로, ${change.gainCm.oneDecimal()}cm 자랐어요" },
    ) {
        Canvas(Modifier.width(22.dp).fillMaxHeight()) {
            val step = 9.dp.toPx()
            var y = size.height
            var i = 0
            while (y > 0) {
                drawLine(cs.outline, Offset(size.width - if (i % 5 == 0) 14.dp.toPx() else 7.dp.toPx(), y), Offset(size.width, y), 1.dp.toPx())
                y -= step
                i++
            }
        }
        Text(
            "+${change.gainCm.oneDecimal()}cm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = cs.secondary,
            modifier = Modifier.padding(start = 34.dp, top = 12.dp).background(cs.secondaryContainer, RoundedCornerShape(99.dp)).padding(horizontal = 12.dp, vertical = 4.dp),
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight().padding(start = 40.dp, end = 16.dp, top = 52.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
            listOf(Triple(change.fromCm, "처음", false), Triple(change.toCm, "지금", true)).forEach { (cm, label, now) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(cm.oneDecimal(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = cs.onSurface)
                    Box(Modifier.width(46.dp).height((BAR_DP * (cm - low) / span).dp).background(if (now) cs.secondary else cs.secondaryContainer, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)))
                    Text(label, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                }
            }
        }
    }
}

private const val FLOOR_CM = 8.0
private const val BAR_DP = 90.0
