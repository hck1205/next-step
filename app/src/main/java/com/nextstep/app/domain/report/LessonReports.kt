package com.nextstep.app.domain.report

import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.domain.export.ExportDoc
import com.nextstep.app.domain.feedback.FeedbackAudience
import com.nextstep.app.domain.feedback.FeedbackEngine
import com.nextstep.app.domain.feedback.FeedbackVoice
import com.nextstep.app.domain.mentor.AssignmentStats
import com.nextstep.app.domain.period.PeriodKind
import com.nextstep.app.domain.period.Periods
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/**
 * 멘토 → 학부모 수업 리포트: 담당 과목의 이번 주 공부 · 진도 · 과제 · 살펴본 것을 한 장으로 모아 카톡·문자로 보냅니다.
 * 숫자는 아이 자신의 기록만(또래·평균 없음). 선생님 한마디는 보낼 때 덧붙입니다([text]).
 */
object LessonReports {

    fun of(input: LessonReportInput): LessonReport {
        val subjects = input.subjects.joinToString("·") { it.name }.ifBlank { "전 과목" }
        val span = if (input.kind == ReportKind.MONTH) input.span else DateUtils.formatDay(input.today)
        val title = "${input.studentName.ifBlank { "학생" }} $subjects ${if (input.kind == ReportKind.MONTH) "월간 " else ""}수업 리포트 · $span"
        return LessonReport(title, "${input.mentorName.ifBlank { "담당" }} 선생님", listOfNotNull(study(input), progress(input), assignments(input), notes(input)))
    }

    /** 이번 주 수업 리포트(이번 주 담당 과목 공부 시간 · 진도 · 과제 · 살펴본 것). 담당 과목이 없으면 null. */
    fun week(study: MentorStudy, today: LocalDate): LessonReport? {
        val minutes = StudyStats.weeklyMinutesBySubject(study.sessions, study.subjects, today).filter { it.subject != null }.sumOf { it.minutes }
        return mentorReport(study, ReportKind.WEEK, "이번 주", minutes, study.tasks, today)
    }

    /** 이번 달 수업 리포트: 이번 달 공부 시간과 이번 달 마감인 과제로. 담당 과목이 없으면 null. */
    fun month(study: MentorStudy, today: LocalDate): LessonReport? {
        val month = Periods.current(PeriodKind.MONTH, today)
        val minutes = study.sessions.filter { DateUtils.toLocalDate(it.startAt) in month }.sumOf { it.durationMinutes }
        val tasks = study.tasks.filter { DateUtils.fromEpochDay(it.dueDate) in month }
        return mentorReport(study, ReportKind.MONTH, "${today.monthValue}월", minutes, tasks, today)
    }

    /** 멘토 범위의 사실을 학부모가 읽을 말로(같은 사실 — 멘토 범위 — 을 학부모의 말로 옮김). */
    private fun mentorReport(study: MentorStudy, kind: ReportKind, span: String, minutes: Int, tasks: List<TaskEntity>, today: LocalDate): LessonReport? {
        if (study.subjects.isEmpty()) return null
        val notes = FeedbackEngine.forAudience(study.findings, FeedbackAudience.MENTOR).map { FeedbackVoice.line(it, FeedbackAudience.PARENT) }
        return of(
            LessonReportInput(
                study.studentName, study.mentorName, study.subjects, today, minutes, study.progress,
                AssignmentStats.report(tasks, study.subjects, today), notes, kind, span,
            ),
        )
    }

    /** PDF 로 보낼 문서: 덩어리 + (있으면) 선생님 한마디 · 서명. */
    fun doc(report: LessonReport, note: String, signature: String = ""): ExportDoc = ExportDoc(
        report.title, report.from,
        report.sections + listOfNotNull(
            note.trim().takeIf { it.isNotEmpty() }?.let { ReportSection("선생님 한마디", listOf(it)) },
            signature.trim().takeIf { it.isNotEmpty() }?.let { ReportSection("선생님", listOf(it)) },
        ),
        footer = "NextStep에서 보냄",
    )

    /** 보낼 글: 제목 · 보낸 사람 · 덩어리들 · (있으면) 선생님 한마디 · 서명 · 끝맺음. */
    fun text(report: LessonReport, note: String, signature: String = ""): String = buildString {
        appendLine("📘 ${report.title}")
        appendLine(report.from)
        report.sections.forEach { s ->
            appendLine()
            appendLine("■ ${s.label}")
            s.lines.forEach { appendLine("  · $it") }
        }
        note.trim().takeIf { it.isNotEmpty() }?.let { appendLine(); appendLine("■ 선생님 한마디"); appendLine("  $it") }
        signature.trim().takeIf { it.isNotEmpty() }?.let { appendLine(); appendLine(it) }
        appendLine()
        append("— NextStep에서 보냄")
    }

    /** 1. 그 기간의 공부 시간(담당 과목). */
    private fun study(input: LessonReportInput): ReportSection =
        ReportSection("${input.span} 공부", listOf(if (input.minutes == 0) "아직 기록이 없어요" else DateUtils.formatMinutes(input.minutes)))

    /** 2. 진도: 과목마다 수업·복습한 단원. 단원이 없으면 빠집니다. */
    private fun progress(input: LessonReportInput): ReportSection? = input.progress.filter { it.total > 0 }
        .map { "${it.subject.name} ${it.total}단원 중 수업 ${it.classCovered} · 복습 ${it.reviewed}" }
        .takeIf { it.isNotEmpty() }?.let { ReportSection("진도", it) }

    /** 3. 과제: 끝냄·남음·밀림과 곧 마감인 것([MAX_DUE] 개). 낸 과제가 없으면 빠집니다. */
    private fun assignments(input: LessonReportInput): ReportSection? {
        val a = input.assignments
        if (a.total == 0) return null
        val open = a.total - a.done
        val head = "끝냄 ${a.done} · 남음 ${open - a.overdue.size} · 밀림 ${a.overdue.size}"
        val due = (a.overdue + a.dueSoon).take(MAX_DUE).map { "${it.title} — ${DateUtils.formatShortDate(DateUtils.fromEpochDay(it.dueDate))}까지" }
        return ReportSection("과제", listOf(head) + due)
    }

    /** 4. 이번 주 살펴본 것(학부모의 말, [MAX_NOTES] 줄). */
    private fun notes(input: LessonReportInput): ReportSection? =
        input.notes.take(MAX_NOTES).map { it.title }.takeIf { it.isNotEmpty() }?.let { ReportSection(if (input.kind == ReportKind.WEEK) "이번 주 살펴본 것" else "최근 살펴본 것", it) }

    private const val MAX_DUE = 3
    private const val MAX_NOTES = 2
}
