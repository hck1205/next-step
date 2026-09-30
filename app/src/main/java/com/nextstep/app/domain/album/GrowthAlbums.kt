package com.nextstep.app.domain.album

import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.domain.cheer.CheerKind
import com.nextstep.app.domain.export.ExportDoc
import com.nextstep.app.domain.period.Period
import com.nextstep.app.domain.period.PeriodKind
import com.nextstep.app.domain.period.PeriodRecords
import com.nextstep.app.domain.period.PeriodReports
import com.nextstep.app.domain.period.PeriodStats
import com.nextstep.app.domain.report.ReportSection
import com.nextstep.app.domain.school.SchoolCalendar
import com.nextstep.app.domain.stats.TrendStats
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * 올해의 성장 앨범(가족 플러스로 나눌 수 있는 기능 — 지금은 모두에게 열림): 한 학년도(3월~2월)를 좋았던 것으로 한 권에.
 * 한 해 숫자 · 이룬 목표 · 주말 이야기에서 자랑한 것 · 해 본 것 · 자란 키 · 기다렸던 것. 점수·비교·밀린 것은 넣지 않습니다.
 */
object GrowthAlbums {

    /** [today] 가 속한 학년도, [yearsBack] 만큼 앞의 학년도. */
    fun year(today: LocalDate, yearsBack: Int = 0): Period {
        val (start, end) = SchoolCalendar.yearRange(today.minusYears(yearsBack.toLong()))
        return Period(PeriodKind.TERM, start, end, "${start.year}학년도")
    }

    /** 보낼 글만 필요할 때: 학년도 끝까지의 앨범 한 권을 글로([doc]). */
    fun album(studentName: String, year: Period, r: PeriodRecords, plans: List<WeekPlanEntity>, growth: List<GrowthRecordEntity>): ExportDoc =
        doc(book(studentName, year, r, plans, growth, year.end), PeriodReports.stats(year, r))

    /** 앨범 한 권을 할머니·할아버지께 보낼 글로: 한 해 숫자([stats]) · 이룬 목표 · 자랑 · 해 본 것 · 자란 키 · 기다렸던 것. 화면과 같은 한 권에서 만듭니다. */
    fun doc(book: GrowthAlbum, stats: PeriodStats): ExportDoc {
        val sections = listOfNotNull(
            ReportSection("한 해 숫자", listOf("공부 ${DateUtils.formatMinutes(stats.studyMinutes)} · 공부한 날 ${stats.studyDays}일", "해낸 일 ${stats.tasksDone}개 · 이룬 목표 ${stats.goalsDone}개 · 받은 응원 ${stats.cheers}개")),
            section("이룬 목표", book.goals.map { it.title }),
            section("자랑하고 싶었던 순간", book.talks.filter { it.proud.isNotBlank() }.map { "${DateUtils.formatShortDate(it.week)} 주 · ${it.proud}" }.takeLast(MAX_LINES)),
            section("해 본 것", book.activities.map { "${it.type.label} · ${it.title}" }),
            book.height?.let { ReportSection("자란 키", listOf("${cm(it.fromCm)}cm → ${cm(it.toCm)}cm (+${cm(it.gainCm)}cm)")) },
            section("기다렸던 것", book.talks.map { it.wish }.filter { it.isNotBlank() }.distinct()),
        )
        return ExportDoc("${book.studentName}의 ${book.year.label} 성장 앨범", "${DateUtils.formatMonth(book.year.start)} – ${DateUtils.formatMonth(book.year.end)}", sections)
    }

    private fun section(label: String, lines: List<String>): ReportSection? = lines.takeIf { it.isNotEmpty() }?.let { ReportSection(label, it.take(MAX_LINES)) }

    /**
     * 화면이 장마다 그릴 앨범 한 권. [today] 까지만(앞날 없음) — 공부 달력은 학년도 첫 주부터 [today] 가 든 주(지난 학년도면 마지막 주)까지.
     */
    fun book(studentName: String, year: Period, r: PeriodRecords, plans: List<WeekPlanEntity>, growth: List<GrowthRecordEntity>, today: LocalDate): GrowthAlbum {
        val until = minOf(today, year.end)
        val studied = r.sessions.filter { !it.deleted }.map { DateUtils.toLocalDate(it.startAt) }.filter { it in year && !it.isAfter(until) }.toSortedSet()
        return GrowthAlbum(
            studentName = studentName.ifBlank { "우리 아이" }, year = year,
            goals = r.goals.filter { !it.deleted && it.status == GoalStatus.DONE && it.doneAt != null }
                .map { AlbumGoal(it.title, DateUtils.toLocalDate(it.doneAt!!)) }.filter { it.doneOn in year }.sortedBy { it.doneOn },
            studyDays = studied.size, longestStreak = longestStreak(studied),
            studyWeeks = TrendStats.heatCalendar(r.sessions.filter { !it.deleted }, until, weeksBetween(year.start, until)),
            activities = r.activities.filter { !it.deleted && DateUtils.fromEpochDay(it.date) in year }.sortedBy { it.date }
                .map { AlbumActivity(it.title, it.type, DateUtils.fromEpochDay(it.date)) },
            height = heightChange(year, growth),
            cheers = r.cheers.filter { !it.deleted && DateUtils.toLocalDate(it.createdAt) in year }.sortedBy { it.createdAt }
                .map { AlbumCheer(it.fromName.ifBlank { "가족" }, it.taskTitle, CheerKind.from(it.kind), DateUtils.toLocalDate(it.createdAt)) },
            talks = plans.filter { !it.deleted && it.talkAt != null && DateUtils.fromEpochDay(it.weekStart) in year && (it.proud.isNotBlank() || it.wish.isNotBlank()) }
                .sortedBy { it.weekStart }.map { AlbumTalk(DateUtils.fromEpochDay(it.weekStart), it.proud, it.wish, it.treat) },
        )
    }

    /** 가장 길게 이어서 공부한 날 수. */
    internal fun longestStreak(days: Set<LocalDate>): Int {
        var best = 0
        var run = 0
        var prev: LocalDate? = null
        days.sorted().forEach { d ->
            run = if (prev?.plusDays(1) == d) run + 1 else 1
            best = maxOf(best, run)
            prev = d
        }
        return best
    }

    private fun weeksBetween(start: LocalDate, until: LocalDate): Int =
        (ChronoUnit.WEEKS.between(DateUtils.weekStart(start), DateUtils.weekStart(until)).toInt() + 1).coerceAtLeast(1)

    private fun heightChange(year: Period, growth: List<GrowthRecordEntity>): HeightChange? {
        val list = growth.filter { !it.deleted && it.heightCm != null && DateUtils.fromEpochDay(it.date) in year }.sortedBy { it.date }
        return if (list.size < 2) null else HeightChange(list.first().heightCm!!, list.last().heightCm!!)
    }

    /** 키를 적는 모양(141 · 138.2). */
    fun cm(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else String.format(Locale.ROOT, "%.1f", v)

    private const val MAX_LINES = 12
}
