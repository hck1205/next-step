package com.nextstep.app.domain.mentor

import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** 메모·피드백을 주 단위로 묶습니다(최신 주 먼저, 주 안에서도 최신 먼저). */
object FeedbackLog {
    fun weeks(notes: List<NoteEntity>, today: LocalDate): List<FeedbackWeek> {
        val thisWeek = DateUtils.weekStart(today)
        return notes.filter { !it.deleted }
            .groupBy { ChronoUnit.WEEKS.between(DateUtils.weekStart(DateUtils.toLocalDate(it.createdAt)), thisWeek).toInt() }
            .toSortedMap()
            .map { (ago, list) -> FeedbackWeek(label(ago), list.sortedByDescending { it.createdAt }) }
    }

    fun label(weeksAgo: Int): String = when {
        weeksAgo <= 0 -> "이번 주"
        weeksAgo == 1 -> "지난주"
        else -> "${weeksAgo}주 전"
    }
}
