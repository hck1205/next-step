package com.nextstep.app.ui.components.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Toys
import androidx.compose.ui.graphics.vector.ImageVector
import com.nextstep.app.domain.project.RoutineKind

/** 교육 프로젝트 루틴 종류의 아이콘. 오늘 카드와 프로젝트 화면이 같은 아이콘을 씁니다. */
fun routineIcon(kind: RoutineKind): ImageVector = when (kind) {
    RoutineKind.LISTEN -> Icons.Default.Headphones
    RoutineKind.READ -> Icons.AutoMirrored.Filled.MenuBook
    RoutineKind.SPEAK -> Icons.Default.RecordVoiceOver
    RoutineKind.WRITE -> Icons.Default.Edit
    RoutineKind.PLAY -> Icons.Default.Toys
    RoutineKind.PRACTICE -> Icons.Default.FitnessCenter
    RoutineKind.REVIEW -> Icons.Default.Replay
}
