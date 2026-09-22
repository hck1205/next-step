package com.nextstep.app.domain.planner

import java.time.LocalTime

data class PlanOptions(
    val days: Int = 7,
    val startTime: LocalTime = LocalTime.of(19, 0),
    val sessionMinutes: Int = 50,
    val breakMinutes: Int = 10,
    val sessionsPerDay: Int = 2,
    val includeWeekend: Boolean = true,
)
