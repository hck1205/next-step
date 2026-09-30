package com.nextstep.app.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 살짝 기울어진 스티커 한 장: 그림 · 큰 숫자와 단위 · 이름(예: 4개 해낸 일). [tilt] 는 도(°). */
@Composable
fun StickerTile(icon: ImageVector, value: String, unit: String, label: String, tint: Color, container: Color, tilt: Float, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier.rotate(tilt).shadow(2.dp, shape).background(cs.surface, shape).border(1.dp, cs.outlineVariant, shape).padding(14.dp)
            .clearAndSetSemantics { contentDescription = "$label $value$unit" },
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(Modifier.size(36.dp).background(container, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp), color = tint)
            Text(unit, style = MaterialTheme.typography.titleSmall, color = tint, modifier = Modifier.padding(start = 2.dp, bottom = 5.dp))
        }
        Text(label, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
    }
}
