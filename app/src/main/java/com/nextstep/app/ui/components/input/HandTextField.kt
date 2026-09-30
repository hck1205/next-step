package com.nextstep.app.ui.components.input

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstep.app.ui.theme.handStyle

/** 공책에 적듯 쓰는 한 줄: 위에 작은 이름표, 손글씨 글자, 아래에 점선 밑줄. */
@Composable
fun HandTextField(value: String, onChange: (String) -> Unit, label: String, placeholder: String, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        BasicTextField(
            value = value, onValueChange = onChange, singleLine = true,
            textStyle = handStyle(28.sp).copy(color = cs.onSurface), cursorBrush = SolidColor(cs.primary),
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = label }.drawBehind {
                val y = size.height - 1.dp.toPx()
                drawLine(cs.outline, Offset(0f, y), Offset(size.width, y), strokeWidth = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH, DASH)))
            },
            decorationBox = { inner ->
                Box(Modifier.padding(vertical = 6.dp)) {
                    if (value.isEmpty()) Text(placeholder, style = handStyle(28.sp), color = cs.onSurfaceVariant.copy(alpha = HINT_ALPHA))
                    inner()
                }
            },
        )
    }
}

private const val DASH = 10f
private const val HINT_ALPHA = 0.5f
