package com.nextstep.app.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextstep.app.ui.components.card.LocalFlatCards

/**
 * 오늘 화면 슬라이드의 한 장. 한 줄의 타일은 모두 같은 높이라 넘겨도 들쭉날쭉하지 않습니다.
 * 위: 제목(카드가 스스로 머리를 가지면 [title] 을 비움) · 가운데: 카드 내용을 바탕 없이([LocalFlatCards]) · 아래 같은 자리: "자세히"(자세히 시트).
 * 내용이 높이를 넘으면 아래를 흐리게 잘라 "자세히"로 이어 보게 합니다. 글씨를 키운 학생 화면은 그만큼 타일도 커집니다.
 */
@Composable
fun SlideCard(title: String, onMore: () -> Unit, content: @Composable () -> Unit) {
    val scale = LocalDensity.current.fontScale.coerceIn(1f, MAX_SCALE)
    val surface = MaterialTheme.colorScheme.surface
    Card(
        modifier = Modifier.fillMaxWidth().height((SLIDE_HEIGHT * scale).dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)) {
            if (title.isNotBlank()) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(bottom = 8.dp))
            }
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
                val room = constraints.maxHeight
                var overflows by remember { mutableStateOf(false) }
                Box(Modifier.fillMaxWidth().wrapContentHeight(align = Alignment.Top, unbounded = true).onSizeChanged { overflows = it.height > room }) {
                    CompositionLocalProvider(LocalFlatCards provides true) { content() }
                }
                if (overflows) {
                    Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(FADE.dp).background(Brush.verticalGradient(listOf(Color.Transparent, surface))))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onMore) {
                    Text("자세히", style = MaterialTheme.typography.labelLarge)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

/** 타일 높이(dp). 줄인 카드의 세 줄 + 제목 + "자세히"가 들어가는 높이. */
private const val SLIDE_HEIGHT = 224
/** 글씨를 키운 화면에서 타일이 커지는 한도. */
private const val MAX_SCALE = 1.4f
/** 넘칠 때 아래를 흐리게 하는 높이(dp). */
private const val FADE = 40
