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
import com.nextstep.app.ui.theme.FamilyKindColors

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

/** 가족 일정 종류의 색(테마의 [FamilyKindColors]). 달력 칸의 점과 아이콘 바탕에 씁니다. */
fun familyKindColor(kind: FamilyEventKind): Color = FamilyKindColors.getValue(kind)
