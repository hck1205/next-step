package com.nextstep.app.domain.period

import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.domain.export.ExportDoc
import com.nextstep.app.domain.report.ReportSection
import com.nextstep.app.domain.school.SchoolCalendar
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

/**
 * 월간·학기 리포트(가족 플러스로 나눌 수 있는 기능 — 지금은 모두에게 열림). 이 기간과 바로 앞 기간을 견주되 비교는 아이 자신과만.
 * 긴 흐름은 학년도(3월~)의 달마다 공부 시간과 할 일을 끝낸 비율.
 */
object PeriodReports {

    fun stats(period: Period, r: PeriodRecords): PeriodStats {
        val sessions = r.sessions.filter { !it.deleted && DateUtils.toLocalDate(it.startAt) in period }
        val due = r.tasks.filter { !it.deleted && DateUtils.fromEpochDay(it.dueDate) in period }
        return PeriodStats(
            studyMinutes = sessions.sumOf { it.durationMinutes },
            studyDays = sessions.map { DateUtils.toLocalDate(it.startAt) }.toSet().size,
            tasksDue = due.size, tasksDone = due.count { it.done },
            scores = scores(period, r),
            activities = r.activities.count { !it.deleted && DateUtils.fromEpochDay(it.date) in period },
            goalsDone = r.goals.count { !it.deleted && it.status == GoalStatus.DONE && it.doneAt?.let { d -> DateUtils.toLocalDate(d) in period } == true },
            cheers = r.cheers.count { !it.deleted && DateUtils.toLocalDate(it.createdAt) in period },
        )
    }

    /** 리포트 한 편(글·PDF 같은 모양). 앞 기간 값은 괄호로 붙입니다. */
    fun report(studentName: String, period: Period, now: PeriodStats, before: PeriodStats): ExportDoc {
        val prev = period.kind.previousLabel
        val sections = listOfNotNull(
            ReportSection("공부", listOf("공부 ${DateUtils.formatMinutes(now.studyMinutes)} ($prev ${DateUtils.formatMinutes(before.studyMinutes)})", "공부한 날 ${now.studyDays}일 ($prev ${before.studyDays}일)")),
            now.doneRate?.let { rate -> ReportSection("할 일", listOf("마감 할 일 ${now.tasksDue}개 중 ${now.tasksDone}개 끝냄 · $rate%" + (before.doneRate?.let { " ($prev $it%)" } ?: ""))) },
            now.scores.takeIf { it.isNotEmpty() }?.let { s -> ReportSection("과목 점수(평균)", s.map { (name, v) -> "$name ${v}점" + (before.scores[name]?.let { " ($prev ${it}점)" } ?: "") }) },
            ReportSection("해 본 것", listOf("활동 ${now.activities}개 · 이룬 목표 ${now.goalsDone}개 · 받은 응원 ${now.cheers}개")),
        )
        return ExportDoc("${studentName.ifBlank { "우리 아이" }}의 ${period.kind.label} 리포트", period.label, sections)
    }

    /** 학년도(3월)부터 이번 달까지 달마다. */
    fun monthlySeries(r: PeriodRecords, today: LocalDate): List<MonthPoint> {
        val from = YearMonth.from(SchoolCalendar.yearRange(today).first)
        val months = generateSequence(from) { it.plusMonths(1) }.takeWhile { !it.isAfter(YearMonth.from(today)) }.toList()
        return months.map { ym ->
            val s = stats(Period(PeriodKind.MONTH, ym.atDay(1), ym.atEndOfMonth(), ""), r)
            MonthPoint("${ym.monthValue}월", s.studyMinutes, s.doneRate)
        }
    }

    private fun scores(period: Period, r: PeriodRecords): Map<String, Int> = r.subjects.mapNotNull { s ->
        val list = r.grades.filter { !it.deleted && it.subjectId == s.id && DateUtils.fromEpochDay(it.date) in period }
        if (list.isEmpty()) null else s.name to list.map { it.percent }.average().roundToInt()
    }.toMap()


}
