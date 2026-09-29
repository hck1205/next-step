package com.nextstep.app.domain.notice

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

/** 알림 시간표: 매일 아침 [MORNING] 한 번, 일요일 저녁 [WEEKEND] 한 번. 그 밖의 때에는 보내지 않습니다(밤·수업 중 조용히). */
object NoticeSchedule {
    val MORNING: LocalTime = LocalTime.of(7, 30)
    val WEEKEND: LocalTime = LocalTime.of(19, 0)
    val WEEKEND_DAY: DayOfWeek = DayOfWeek.SUNDAY

    /** [after] 뒤로 가장 먼저 오는 알림 때. */
    fun next(after: LocalDateTime): NoticeSlot {
        val morning = LocalDateTime.of(after.toLocalDate(), MORNING).let { if (it.isAfter(after)) it else it.plusDays(1) }
        var weekend = LocalDateTime.of(after.toLocalDate(), WEEKEND)
        while (weekend.dayOfWeek != WEEKEND_DAY || !weekend.isAfter(after)) weekend = weekend.plusDays(1)
        return if (weekend.isBefore(morning)) NoticeSlot(weekend, NoticeKind.WEEKEND) else NoticeSlot(morning, NoticeKind.MORNING)
    }
}
