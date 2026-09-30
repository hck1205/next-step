package com.nextstep.app.domain.notice

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class NoticeScheduleTest {
    @Test
    fun nextSlotIsMorningDailyAndSundayEvening() {
        // 2029-03-07 수요일
        assertEquals(NoticeSlot(LocalDateTime.of(2029, 3, 7, 7, 30), NoticeKind.MORNING), NoticeSchedule.next(LocalDateTime.of(2029, 3, 7, 6, 0)))
        assertEquals(NoticeSlot(LocalDateTime.of(2029, 3, 8, 7, 30), NoticeKind.MORNING), NoticeSchedule.next(LocalDateTime.of(2029, 3, 7, 7, 30)))
        // 일요일(3/11) 아침 뒤 → 그날 저녁 주말 이야기, 저녁 뒤 → 월요일 아침
        assertEquals(NoticeSlot(LocalDateTime.of(2029, 3, 11, 19, 0), NoticeKind.WEEKEND), NoticeSchedule.next(LocalDateTime.of(2029, 3, 11, 8, 0)))
        assertEquals(NoticeSlot(LocalDateTime.of(2029, 3, 12, 7, 30), NoticeKind.MORNING), NoticeSchedule.next(LocalDateTime.of(2029, 3, 11, 19, 0)))
        assertEquals(NoticeKind.MORNING, NoticeKind.from("x")); assertEquals(NoticeKind.WEEKEND, NoticeKind.from("WEEKEND"))
    }
}
