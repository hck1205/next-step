package com.nextstep.app.domain.lesson

import com.nextstep.app.data.local.entity.LessonEntity
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * 수업·출결·수업료(튜터 Pro 로 나눌 수 있는 기능 — 지금은 모두 열림). 정해진 요일의 수업은 일정에서 계산하고, 출결만 기록(LessonEntity)으로 남깁니다.
 * 정해진 요일이 아닌 날의 기록(보강 수업)도 그 달 수업에 들어갑니다.
 */
object Lessons {

    fun month(plan: LessonPlan, records: List<LessonEntity>, month: YearMonth): List<LessonDay> {
        val byDate = records.filter { !it.deleted }.associateBy { DateUtils.fromEpochDay(it.date) }
        val planned = (1..month.lengthOfMonth()).map { month.atDay(it) }.filter { it.dayOfWeek in plan.days }
        val extra = byDate.keys.filter { YearMonth.from(it) == month && it !in planned }
        return (planned.map { d -> day(d, byDate[d], extra = false) } + extra.map { d -> day(d, byDate[d], extra = true) }).sortedBy { it.date }
    }

    fun summary(days: List<LessonDay>): LessonSummary =
        LessonSummary(days.size, days.count { it.status == LessonStatus.DONE }, days.count { it.status == LessonStatus.ABSENT }, days.count { it.status == LessonStatus.MAKEUP })

    /** 오늘 수업(정해진 요일이거나 오늘 기록이 있으면). */
    fun today(plan: LessonPlan, records: List<LessonEntity>, today: LocalDate): LessonDay? = month(plan, records, YearMonth.from(today)).firstOrNull { it.date == today }

    /** 다음 수업료 받을 날(수업료와 받을 날이 정해져 있을 때). 그 달에 그날이 없으면 말일. */
    fun tuition(plan: LessonPlan, today: LocalDate): TuitionDue? {
        if (plan.fee <= 0 || plan.feeDay !in 1..MAX_DAY) return null
        fun on(ym: YearMonth) = ym.atDay(minOf(plan.feeDay, ym.lengthOfMonth()))
        val thisMonth = on(YearMonth.from(today))
        val date = if (thisMonth.isBefore(today)) on(YearMonth.from(today).plusMonths(1)) else thisMonth
        return TuitionDue(date, ChronoUnit.DAYS.between(today, date).toInt(), plan.fee)
    }

    /** 알림·카드에 띄울 수업료 한 줄: 받을 날 [TUITION_HEADS_UP] 일 전부터. 멘토는 "받을 날", 학부모([payer])는 "낼 날". */
    fun tuitionLine(plan: LessonPlan, today: LocalDate, payer: Boolean = false): String? = tuition(plan, today)?.let { tuitionLine(it, payer) }

    fun tuitionLine(due: TuitionDue, payer: Boolean = false): String? = due.takeIf { it.daysLeft <= TUITION_HEADS_UP }?.let {
        "수업료 ${if (payer) "낼" else "받을"} 날 ${if (it.daysLeft == 0) "오늘" else "D-${it.daysLeft}"} · ${"%,d".format(it.fee)}원"
    }

    /** 수업을 맡은 멘토마다 한 달 수업(일정이 있거나 그 달 기록이 있는 멘토만). */
    fun books(members: List<MemberEntity>, records: List<LessonEntity>, month: YearMonth, today: LocalDate): List<LessonBook> =
        members.filter { it.isMentor }.mapNotNull { m ->
            val plan = LessonPlan.of(m)
            val days = month(plan, records.filter { it.mentorId == m.id }, month)
            if (!plan.isSet && days.isEmpty()) null else LessonBook(m.id, m.name, plan, days, summary(days), tuition(plan, today))
        }

    private fun day(date: LocalDate, record: LessonEntity?, extra: Boolean) = LessonDay(date, LessonStatus.from(record?.status), extra, record?.note.orEmpty())

    const val TUITION_HEADS_UP = 3
    private const val MAX_DAY = 31
}
