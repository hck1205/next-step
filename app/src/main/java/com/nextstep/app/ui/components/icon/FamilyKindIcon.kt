package com.nextstep.app.ui.components.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.nextstep.app.domain.familycalendar.FamilyEventKind

/** 가족 일정 종류의 아이콘. 달력 칸·일정 줄·입력창이 같은 것을 씁니다. */
fun familyKindIcon(kind: FamilyEventKind): ImageVector = when (kind) {
    FamilyEventKind.FAMILY -> Icons.Default.Groups
    FamilyEventKind.OUTING -> Icons.Default.FlightTakeoff
    FamilyEventKind.CELEBRATION -> Icons.Default.Cake
    FamilyEventKind.HOSPITAL -> Icons.Default.LocalHospital
    FamilyEventKind.SCHOOL -> Icons.Default.School
    FamilyEventKind.LESSON -> Icons.AutoMirrored.Filled.MenuBook
    FamilyEventKind.WORK -> Icons.Default.Work
    FamilyEventKind.PROMISE -> Icons.Default.EventAvailable
    FamilyEventKind.HOME -> Icons.Default.Home
}

/** 가족 일정 종류의 색(밝은·어두운 바탕 모두에서 읽히는 중간 밝기). 달력 칸의 점과 아이콘 바탕에 씁니다. */
fun familyKindColor(kind: FamilyEventKind): Color = Color(
    when (kind) {
        FamilyEventKind.FAMILY -> 0xFF0E8A5F
        FamilyEventKind.OUTING -> 0xFF2B7DE9
        FamilyEventKind.CELEBRATION -> 0xFFD9467A
        FamilyEventKind.HOSPITAL -> 0xFFD64545
        FamilyEventKind.SCHOOL -> 0xFF4F46E5
        FamilyEventKind.LESSON -> 0xFF7C4DDB
        FamilyEventKind.WORK -> 0xFF64748B
        FamilyEventKind.PROMISE -> 0xFFC2610C
        FamilyEventKind.HOME -> 0xFF8A6D3B
    },
)
