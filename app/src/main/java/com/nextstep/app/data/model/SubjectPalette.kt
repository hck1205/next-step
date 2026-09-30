package com.nextstep.app.data.model

/** 과목 색. 교과 이름이면 늘 같은 색, 그 밖에는 이름 해시로 고정 팔레트에서 고릅니다. */
object SubjectPalette {
    private val named: Map<String, Long> = mapOf(
        "국어" to 0xFFEF4444, "수학" to 0xFF3B82F6, "영어" to 0xFF10B981, "과학" to 0xFF8B5CF6, "사회" to 0xFFF59E0B,
        "역사" to 0xFFB45309, "한국사" to 0xFFB45309, "통합교과" to 0xFF0EA5E9, "통합사회" to 0xFFF59E0B, "통합과학" to 0xFF8B5CF6,
    )
    private val fallback: List<Long> = listOf(0xFF0EA5E9, 0xFFEC4899, 0xFF14B8A6, 0xFFF97316, 0xFF6366F1, 0xFF84CC16)

    fun colorFor(name: String): Long = named[name.trim()] ?: fallback[Math.floorMod(name.trim().hashCode(), fallback.size)]
}
