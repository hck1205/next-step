package com.nextstep.app.domain.lesson

import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.domain.time.DateUtils
import java.time.DayOfWeek

/**
 * 멘토의 수업 일정과 수업료: 매주 [days] 요일 [startMinute]~[endMinute], 한 달 [fee] 원을 매달 [feeDay] 일에.
 * 멘토 구성원 행에 저장합니다(학부모도 봅니다).
 */
data class LessonPlan(val days: Set<DayOfWeek>, val startMinute: Int, val endMinute: Int, val fee: Int = 0, val feeDay: Int = 0) {
    val isSet: Boolean get() = days.isNotEmpty()

    /** "매주 화·목 16:00–17:30". */
    val label: String get() = if (!isSet) "수업 일정 없음" else
        "매주 ${days.sorted().joinToString("·") { DateUtils.dayOfWeekLabel(it) }} ${DateUtils.formatClock(startMinute)}–${DateUtils.formatClock(endMinute)}"

    companion object {
        fun of(member: MemberEntity): LessonPlan = LessonPlan(
            days = member.lessonDays.split(",").mapNotNull { it.trim().toIntOrNull()?.takeIf { d -> d in 1..DAYS }?.let(DayOfWeek::of) }.toSet(),
            startMinute = member.lessonStart, endMinute = member.lessonEnd, fee = member.tuitionFee, feeDay = member.tuitionDay,
        )

        /** 일정이 없는 멘토. */
        val NONE = LessonPlan(emptySet(), 0, 0)

        /** 저장할 요일 글("2,4"). */
        fun encodeDays(days: Set<DayOfWeek>): String = days.map { it.value }.sorted().joinToString(",")

        private const val DAYS = 7

    }
}
