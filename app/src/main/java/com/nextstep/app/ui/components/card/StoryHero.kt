package com.nextstep.app.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstep.app.ui.theme.handStyle

/** 가족의 순간을 여는 머리 카드: 부드러운 두 빛깔 바탕 위에 손글씨 질문 한 줄 + 안내 + 작은 그림. */
@Composable
fun StoryHero(title: String, hint: String, icon: ImageVector, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val bg = Brush.linearGradient(listOf(cs.primaryContainer, cs.secondaryContainer))
    Row(
        modifier.fillMaxWidth().background(bg, RoundedCornerShape(22.dp)).padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = handStyle(34.sp), color = cs.onSurface, modifier = Modifier.semantics { heading() })
            Text(hint, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
        }
        Icon(icon, contentDescription = null, tint = cs.tertiary, modifier = Modifier.size(28.dp))
    }
}
