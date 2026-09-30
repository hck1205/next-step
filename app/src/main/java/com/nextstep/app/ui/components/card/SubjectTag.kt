package com.nextstep.app.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.ui.theme.subjectColor

@Composable
fun SubjectTag(subject: SubjectEntity?, modifier: Modifier = Modifier) {
    val color = subject?.let { subjectColor(it.color) } ?: MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier.background(color.copy(alpha = TAG_ALPHA), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ColorDot(color, 8)
        Spacer(Modifier.width(5.dp))
        Text(subject?.name ?: "과목 없음", style = MaterialTheme.typography.labelMedium, color = color)
    }
}

private const val TAG_ALPHA = 0.14f
