package com.nextstep.app.domain.notice

import com.nextstep.app.domain.familycalendar.FamilyCalendar
import com.nextstep.app.domain.familycalendar.FamilyEventKind
import com.nextstep.app.domain.journey.JourneyPhase
import com.nextstep.app.domain.lesson.Lessons
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 알림 문장 한 곳. 하루에 한 장으로 모으고(여러 번 울리지 않음), 챙길 것이 없으면 보내지 않습니다.
 * 규칙 하나 = 함수 하나: 가족 일정 → 수업·수업료 → 시험 → 시험·목표 단계 → 여정(학부모) → 할 일 순으로 [MAX_LINES] 줄까지.
 * 주말 알림은 기대를 여는 말로만(밀린 것·점수는 넣지 않음).
 */
object NoticeComposer {

    fun morning(input: NoticeInput, today: LocalDate): Notice? {
        val lines = (family(input, today) + lessons(input) + exams(input, today) + missions(input, today) + journey(input, today) + tasks(input)).take(MAX_LINES)
        if (lines.isEmpty()) return null
        val title = if (input.isStudent) "오늘 챙길 것 ${lines.size}가지, 하나씩 해 봐요" else "오늘 챙길 것 ${lines.size}가지"
        return Notice(NoticeKind.MORNING, title, lines)
    }

    /** 일요일 저녁: 이번 주 반짝인 순간과 다음 주 기대되는 일을 나누자는 초대. 가족에게만, 이미 나눴으면 보내지 않습니다. */
    fun weekend(input: NoticeInput): Notice? {
        if (!input.family || input.talkDone) return null
        val treat = input.nextWeek.firstOrNull { it.kind in LOOK_FORWARD } ?: input.nextWeek.firstOrNull()
        val lines = listOfNotNull(
            if (input.isStudent) "이번 주 반짝인 순간을 가족과 나눠 봐요" else "아이와 이번 주 반짝인 순간을 나눠 보세요",
            treat?.let { "다음 주 기대되는 일: ${it.event.title}" },
            "다음 주에 해 보고 싶은 것 하나를 골라 봐요",
        )
        return Notice(NoticeKind.WEEKEND, "주말 이야기 시간이에요", lines)
    }

    /** 1. 가족 일정: 오늘 것과 미리 보기에 든 다가오는 것. */
    private fun family(input: NoticeInput, today: LocalDate): List<String> = if (!input.family) emptyList() else
        input.familyAhead.map { o ->
            val time = if (o.event.allDay || !o.isFirstDay) "" else " ${FamilyCalendar.timeLabel(o).substringBefore('–')}"
            "${FamilyCalendar.dayLabel(o.date, today)}$time ${o.event.title}"
        }

    /** 1-1. 수업: 멘토에게 오늘 수업 시각, 어른에게 [Lessons.TUITION_HEADS_UP] 일 안의 수업료(멘토 받을 날 · 학부모 낼 날). */
    private fun lessons(input: NoticeInput): List<String> = if (input.isStudent) emptyList() else
        listOfNotNull(input.lessonAt?.let { "오늘 %02d:%02d 수업".format(it / MINUTES_IN_HOUR, it % MINUTES_IN_HOUR) }) +
            input.tuition.mapNotNull { Lessons.tuitionLine(it, payer = input.family) }

    /** 2. 시험: [EXAM_DAYS] 안의 날(D-7 · D-3 · D-1 · 오늘)에만. */
    private fun exams(input: NoticeInput, today: LocalDate): List<String> = input.exams.mapNotNull { e ->
        val days = ChronoUnit.DAYS.between(today, e.date).toInt()
        if (days in EXAM_DAYS) "${e.title} ${if (days == 0) "오늘" else "D-$days"}" else null
    }

    /** 3. 시험·목표의 다음 단계: 오늘까지거나 밀린 단계가 있는 것만. */
    private fun missions(input: NoticeInput, today: LocalDate): List<String> =
        input.missions.filter { it.overdueSteps > 0 || it.nextStep.dueDate?.let { d -> d <= today.toEpochDay() } == true }
            .map { "${it.goal.title} · 다음: ${it.nextStep.title}" }

    /** 4. 여정(가족): 마감이 [JOURNEY_DAYS] 안이거나 지난 것. */
    private fun journey(input: NoticeInput, today: LocalDate): List<String> = if (!input.family || input.isStudent) emptyList() else
        input.journey.filter { it.phase(today) == JourneyPhase.OVERDUE || ChronoUnit.DAYS.between(today, it.dueDate) in 0..JOURNEY_DAYS }
            .map { "${it.title} 마감 ${if (it.dueDate.isBefore(today)) "지남" else "D-${ChronoUnit.DAYS.between(today, it.dueDate)}"}" }

    /** 5. 할 일: 오늘 마감 수(밀린 것은 어른에게만 함께). */
    private fun tasks(input: NoticeInput): List<String> {
        if (input.dueToday == 0 && (input.isStudent || input.overdue == 0)) return emptyList()
        val late = if (!input.isStudent && input.overdue > 0) " · 밀린 것 ${input.overdue}개" else ""
        return listOf("오늘 할 일 ${input.dueToday}개$late")
    }

    const val MAX_LINES = 4
    private val EXAM_DAYS = setOf(0, 1, 3, 7)
    private const val JOURNEY_DAYS = 7L
    private const val MINUTES_IN_HOUR = 60
    private val LOOK_FORWARD = setOf(FamilyEventKind.OUTING, FamilyEventKind.CELEBRATION, FamilyEventKind.FAMILY)
}
