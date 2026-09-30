package com.nextstep.app.ui.familytalk.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** 이야기 네 걸음을 징검다리로: 지난 걸음은 체크, 지금 걸음은 채운 동그라미, 남은 걸음은 빈 동그라미. 점선으로 잇습니다. */
@Composable
internal fun TalkPath(step: Int) {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().semantics { contentDescription = "${step + 1}/${STEPS.size} ${STEPS[step].second}" }) {
        Canvas(Modifier.fillMaxWidth().padding(horizontal = 36.dp).height(40.dp)) {
            drawLine(cs.outline, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            STEPS.forEachIndexed { i, (icon, label) ->
                val done = i < step
                val cur = i == step
                Column(Modifier.width(72.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        Modifier.size(if (cur) 42.dp else 38.dp).background(if (cur) cs.primary else if (done) cs.primaryContainer else cs.surface, CircleShape)
                            .border(2.dp, if (cur || done) cs.primary else cs.outline, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(if (done) Icons.Filled.Check else icon, contentDescription = null, tint = if (cur) cs.onPrimary else if (done) cs.primary else cs.onSurfaceVariant, modifier = Modifier.size(19.dp))
                    }
                    Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = if (cur) FontWeight.Bold else FontWeight.Normal, color = if (cur) cs.onSurface else cs.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

private val STEPS = listOf(
    Icons.Filled.AutoAwesome to "반짝인 순간", Icons.Filled.EmojiEvents to "자랑", Icons.Filled.Eco to "해 보고 싶은 것", Icons.Filled.Celebration to "가족 즐거움",
)
