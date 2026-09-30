package com.nextstep.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.nextstep.app.domain.familycalendar.FamilyEventKind

val Indigo = Color(0xFF4F46E5)
val IndigoLight = Color(0xFFE0E7FF)
val IndigoDark = Color(0xFF3730A3)
/** 성장 앨범 표지의 짙은 남색(밝은 테마 아래쪽 · 어두운 테마 아래쪽). */
val IndigoDeep = Color(0xFF312E81)
val IndigoNight = Color(0xFF1E1B4B)
val Emerald = Color(0xFF10B981)
val EmeraldLight = Color(0xFFD1FAE5)
val Amber = Color(0xFFF59E0B)
val AmberLight = Color(0xFFFEF3C7)
val Rose = Color(0xFFF43F5E)
val RoseLight = Color(0xFFFFE4E6)
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate500 = Color(0xFF64748B)
val Slate900 = Color(0xFF0F172A)

/** 가족 일정 종류의 색(밝은·어두운 바탕 모두에서 읽히는 중간 밝기). 달력 칸의 점과 아이콘 바탕에 씁니다. */
val FamilyKindColors: Map<FamilyEventKind, Color> = mapOf(
    FamilyEventKind.FAMILY to Color(0xFF0E8A5F),
    FamilyEventKind.OUTING to Color(0xFF2B7DE9),
    FamilyEventKind.CELEBRATION to Color(0xFFD9467A),
    FamilyEventKind.HOSPITAL to Color(0xFFD64545),
    FamilyEventKind.SCHOOL to Indigo,
    FamilyEventKind.LESSON to Color(0xFF7C4DDB),
    FamilyEventKind.WORK to Slate500,
    FamilyEventKind.PROMISE to Color(0xFFC2610C),
    FamilyEventKind.HOME to Color(0xFF8A6D3B),
)

/** 저장된 과목 색(ARGB) → Color. */
fun subjectColor(argb: Long): Color = Color(argb.toInt())

/** 과목 색상 선택지. */
val SubjectPalette: List<Long> = listOf(
    0xFFEF4444, 0xFFF97316, 0xFFF59E0B, 0xFF84CC16, 0xFF10B981,
    0xFF06B6D4, 0xFF3B82F6, 0xFF6366F1, 0xFF8B5CF6, 0xFFEC4899,
)
