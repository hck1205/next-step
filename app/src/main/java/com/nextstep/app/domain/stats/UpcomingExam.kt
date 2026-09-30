package com.nextstep.app.domain.stats

import java.time.LocalDate

data class UpcomingExam(val title: String, val subjectId: String?, val date: LocalDate)
