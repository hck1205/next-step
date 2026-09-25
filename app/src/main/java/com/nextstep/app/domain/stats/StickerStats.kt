package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/** 스티커판 계산. 오늘로 끝나는 [BOARD_DAYS]일(오래된 날 먼저). */
object StickerStats {
    const val BOARD_DAYS = 28
    private const val WEEK_DAYS = 7L

    fun board(sessions: List<StudySessionEntity>, tasks: List<TaskEntity>, activities: List<ActivityEntity>, today: LocalDate): StickerBoard {
        val studied = sessions.filter { !it.deleted }.map { DateUtils.toLocalDate(it.startAt) }.toSet()
        val active = activities.filter { !it.deleted }.map { DateUtils.fromEpochDay(it.date) }.toSet()
        val days = (BOARD_DAYS - 1 downTo 0).map { back ->
            val d = today.minusDays(back.toLong())
            StickerDay(d, d in studied, d in active)
        }
        val weekStart = today.minusDays(WEEK_DAYS - 1).toEpochDay()
        return StickerBoard(
            days = days,
            stickers = days.count { it.hasSticker },
            doneThisWeek = tasks.count { !it.deleted && it.done && it.dueDate in weekStart..today.toEpochDay() },
        )
    }
}
