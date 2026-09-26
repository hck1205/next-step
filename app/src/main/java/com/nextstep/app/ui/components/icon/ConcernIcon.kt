package com.nextstep.app.ui.components.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Palette
import androidx.compose.ui.graphics.vector.ImageVector
import com.nextstep.app.domain.hub.Concern

/** 관심사 아이콘. 기록 탭의 관심사 줄과 "한눈에" 타일이 같은 아이콘을 씁니다. */
fun concernIcon(concern: Concern): ImageVector = when (concern) {
    Concern.OVERVIEW -> Icons.Default.Dashboard
    Concern.STUDY -> Icons.AutoMirrored.Filled.MenuBook
    Concern.LEARN -> Icons.Default.Lightbulb
    Concern.PROJECT -> Icons.Default.Flag
    Concern.EXAMS -> Icons.Default.EmojiEvents
    Concern.CLASS -> Icons.Default.AssignmentTurnedIn
    Concern.GROWTH -> Icons.Default.Height
    Concern.DISCOVER -> Icons.Default.Palette
}
