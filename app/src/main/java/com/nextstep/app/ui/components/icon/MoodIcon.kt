package com.nextstep.app.ui.components.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.ui.graphics.vector.ImageVector

/** 돌아보기 기분(1 힘들었어요 · 2 보통 · 3 좋았어요)의 아이콘과 말. */
fun moodIcon(mood: Int): ImageVector = when (mood) {
    1 -> Icons.Default.SentimentDissatisfied
    2 -> Icons.Default.SentimentNeutral
    else -> Icons.Default.SentimentSatisfied
}

fun moodLabel(mood: Int): String = when (mood) {
    1 -> "힘들었어요"
    2 -> "보통이에요"
    else -> "좋았어요"
}
