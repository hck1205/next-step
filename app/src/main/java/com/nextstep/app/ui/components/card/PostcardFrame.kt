package com.nextstep.app.ui.components.card

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

/**
 * 엽서 틀: 위쪽에 항공 우편 줄무늬(두 빛깔 사선), 안쪽은 가족이 적은 말. [corner] 는 오른쪽 위에 겹쳐 둘 것(우표 등).
 * [onClick] 이 있으면 엽서 전체를 누릅니다.
 */
@Composable
fun PostcardFrame(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, corner: @Composable (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(22.dp)
    val base = modifier.fillMaxWidth().shadow(2.dp, shape).clip(shape).background(cs.surface).border(1.dp, cs.outlineVariant, shape)
    Box(if (onClick != null) base.clickable(onClick = onClick) else base) {
        AirmailStripe()
        Column(Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 24.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
        if (corner != null) Box(Modifier.align(Alignment.TopEnd).padding(top = 20.dp, end = 16.dp)) { corner() }
    }
}

@Composable
private fun AirmailStripe() {
    val a = MaterialTheme.colorScheme.primary
    val b = MaterialTheme.colorScheme.tertiary
    Canvas(Modifier.fillMaxWidth().height(7.dp)) {
        val step = STRIPE.dp.toPx()
        var x = -size.height
        var i = 0
        while (x < size.width) {
            val p = Path().apply {
                moveTo(x, size.height); lineTo(x + size.height, 0f); lineTo(x + size.height + step, 0f); lineTo(x + step, size.height); close()
            }
            drawPath(p, if (i % 2 == 0) a else b)
            x += step * 2
            i++
        }
    }
}

private const val STRIPE = 12
