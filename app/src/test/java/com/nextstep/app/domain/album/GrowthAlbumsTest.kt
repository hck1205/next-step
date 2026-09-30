package com.nextstep.app.domain.album

import com.nextstep.app.data.local.entity.CheerEntity
import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.domain.cheer.CheerKind
import com.nextstep.app.domain.period.PeriodRecords
import com.nextstep.app.domain.period.PeriodReports
import com.nextstep.app.domain.text.compact
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.testing.Fixtures
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GrowthAlbumsTest {
    private val today = LocalDate.of(2029, 11, 20)

    @Test
    fun albumGathersTheSchoolYearsGoodMoments() {
        val year = GrowthAlbums.year(today)
        assertEquals(LocalDate.of(2029, 3, 1), year.start); assertEquals("2029학년도", year.label)
        assertEquals(LocalDate.of(2028, 3, 1), GrowthAlbums.year(today, 1).start)
        val r = PeriodRecords(
            sessions = listOf(Fixtures.session("math", LocalDate.of(2029, 5, 1), LocalTime.of(9, 0), 90)),
            tasks = listOf(Fixtures.task("분수", LocalDate.of(2029, 5, 1), done = true), Fixtures.task("밀림", LocalDate.of(2029, 5, 2))),
            goals = listOf(Fixtures.goal("구구단", status = GoalStatus.DONE).copy(doneAt = DateUtils.toMillis(LocalDate.of(2029, 6, 1), LocalTime.NOON))),
            activities = listOf(Fixtures.activity("과학관", date = LocalDate.of(2029, 5, 5)), Fixtures.activity("작년 소풍", date = LocalDate.of(2028, 5, 5))),
        )
        val plans = listOf(WeekPlanEntity(familyId = "fam", weekStart = LocalDate.of(2029, 9, 3).toEpochDay(), proud = "줄넘기 100개", wish = "자전거 타기", talkAt = 1L))
        val growth = listOf(Fixtures.growth(LocalDate.of(2029, 3, 10), height = 130.0), Fixtures.growth(LocalDate.of(2029, 10, 10), height = 134.5))
        val doc = GrowthAlbums.doc(GrowthAlbums.book("지우", year, r, plans, growth, year.end), PeriodReports.stats(year, r))
        assertEquals("지우의 2029학년도 성장 앨범", doc.title); assertEquals("2029년 3월 – 2030년 2월", doc.subtitle)
        assertEquals(listOf("한 해 숫자", "이룬 목표", "자랑하고 싶었던 순간", "해 본 것", "자란 키", "기다렸던 것"), doc.sections.map { it.label })
        assertEquals(listOf("공부 1시간 30분 · 공부한 날 1일", "해낸 일 1개 · 이룬 목표 1개 · 받은 응원 0개"), doc.sections[0].lines)
        assertEquals(listOf("9/3 주 · 줄넘기 100개"), doc.sections[2].lines)
        assertEquals(listOf("현장학습 · 과학관"), doc.sections[3].lines.map { it.replace(Regex("^[^·]+"), "현장학습 ") })
        assertEquals(listOf("130cm → 134.5cm (+4.5cm)"), doc.sections[4].lines)
        assertTrue(doc.text().lines().none { it.contains("밀림") })
    }

    @Test
    fun bookIsTheSameYearDrawnChapterByChapter() {
        val year = GrowthAlbums.year(today)
        val days = listOf(3L, 4L, 5L, 9L).map { LocalDate.of(2029, 9, 1).plusDays(it) }
        val r = PeriodRecords(
            sessions = days.map { Fixtures.session("math", it, LocalTime.of(9, 0), 30) } + Fixtures.session("math", LocalDate.of(2028, 9, 4), LocalTime.of(9, 0), 30),
            goals = listOf(Fixtures.goal("구구단", status = GoalStatus.DONE).copy(doneAt = DateUtils.toMillis(LocalDate.of(2029, 6, 1), LocalTime.NOON)), Fixtures.goal("진행 중")),
            activities = listOf(Fixtures.activity("과학관", date = LocalDate.of(2029, 5, 5))),
            cheers = listOf(CheerEntity(familyId = "fam", taskId = "t", taskTitle = "분수", kind = "STAR", fromName = "아빠", createdAt = DateUtils.toMillis(LocalDate.of(2029, 5, 2), LocalTime.NOON))),
        )
        val plans = listOf(
            WeekPlanEntity(familyId = "fam", weekStart = LocalDate.of(2029, 9, 3).toEpochDay(), proud = "줄넘기 100개", wish = "자전거 타기", talkAt = 1L),
            WeekPlanEntity(familyId = "fam", weekStart = LocalDate.of(2029, 9, 10).toEpochDay(), talkAt = 1L), // 빈 이야기는 넣지 않음
        )
        val growth = listOf(Fixtures.growth(LocalDate.of(2029, 3, 10), height = 130.0), Fixtures.growth(LocalDate.of(2029, 10, 10), height = 134.5))
        val b = GrowthAlbums.book("", year, r, plans, growth, today)
        assertEquals("우리 아이", b.studentName); assertEquals(listOf("구구단"), b.goals.map { it.title })
        assertEquals(4, b.studyDays); assertEquals(3, b.longestStreak) // 작년 기록은 빠짐, 9/4~9/6 사흘 연속
        assertEquals(LocalDate.of(2029, 2, 26), b.studyWeeks.first().monday); assertEquals(DateUtils.weekStart(today), b.studyWeeks.last().monday)
        assertEquals(4, b.studyWeeks.sumOf { it.activeDays })
        assertEquals(listOf("과학관"), b.activities.map { it.title }); assertEquals(4.5, b.height!!.gainCm, 0.001)
        assertEquals(CheerKind.STAR, b.cheers.single().kind); assertEquals("아빠", b.cheers.single().fromName)
        assertEquals(listOf("줄넘기 100개"), b.talks.map { it.proud }); assertFalse(b.isEmpty)
        assertTrue(GrowthAlbums.book("지우", GrowthAlbums.year(today, 1), PeriodRecords(), emptyList(), emptyList(), today).isEmpty)
        assertEquals("134.5", 134.5.compact()); assertEquals("141", 141.0.compact())
    }
}
