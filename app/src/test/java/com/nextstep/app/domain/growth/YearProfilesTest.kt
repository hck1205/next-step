package com.nextstep.app.domain.growth

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.planner.PlanOptions
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class YearProfilesTest {
    private val today = LocalDate.of(2026, 9, 24)
    private val schoolYears = listOf("a0", "a1", "a2", "a3", "a4", "a5", "a6", "e1", "e2", "e3", "e4", "e5", "e6", "m1", "m2", "m3", "h1", "h2", "h3")
    private fun student(gradeYear: Int = 0, birth: LocalDate? = null) = Fixtures.member(Role.STUDENT, "나", gradeYear = gradeYear, birthDate = birth)

    @Test
    fun oneProfilePerYearFromAgeZeroToTheLastYearOfHighSchool() {
        assertEquals(schoolYears + listOf("u", "g"), YearProfiles.all.map { it.key })
        assertEquals(YearProfiles.all.size, YearProfiles.byKey.size)
        assertEquals(listOf("만 0세", "만 6세", "초1", "중2", "고3"), listOf("a0", "a6", "e1", "m2", "h3").map { YearProfiles.byKey.getValue(it).label })
        YearProfiles.all.forEach { y ->
            assertTrue(y.key, y.kinds.size >= 3); assertTrue(y.key, y.subjects.isNotEmpty()); assertTrue(y.key, y.theme.isNotBlank())
            assertTrue(y.key, y.taskRows in 1..3)
        }
    }

    @Test
    fun studyLoadGrowsAndTextShrinksYearByYear() {
        val years = schoolYears.map { YearProfiles.byKey.getValue(it) }
        years.zipWithNext().forEach { (a, b) ->
            assertTrue("${a.key}→${b.key}", b.dailyMinutes >= a.dailyMinutes)
            assertTrue("${a.key}→${b.key}", b.textScale <= a.textScale)
        }
        // 매년 달라지는 학령기: 하루 권장량이 해마다 늘어남
        val school = years.drop(7)
        assertEquals(school.size, school.map { it.dailyMinutes }.toSet().size)
        assertEquals(listOf(0, 0, 0), years.take(3).map { it.dailyMinutes }) // 만 0~2세: 앉아서 하는 공부 없음
        assertEquals(20, YearProfiles.byKey.getValue("e1").dailyMinutes); assertEquals(210, YearProfiles.byKey.getValue("h3").dailyMinutes)
        assertEquals(100, YearProfiles.byKey.getValue("e1").weekMinutes); assertEquals(1260, YearProfiles.byKey.getValue("h3").weekMinutes)
    }

    @Test
    fun levelsFollowTheSchoolYearAndPreschoolIsSeed() {
        (0..6).forEach { assertEquals(StudentUiLevel.SEED, YearProfiles.forAge(it).level) }
        (1..12).forEach { g -> assertEquals(StudentUiLevel.forGrade(g), YearProfiles.forGrade(g)!!.level) }
        assertEquals("u", YearProfiles.forGrade(14)!!.key); assertEquals("g", YearProfiles.forGrade(18)!!.key); assertNull(YearProfiles.forGrade(0))
        assertEquals(listOf(StudentHomeSection.MISSION), YearProfiles.byKey.getValue("m2").lead) // 첫 지필평가 해: 시험·목표 먼저
    }

    @Test
    fun ofUsesBirthDateThenGrade() {
        assertEquals("a4", YearProfiles.of(student(birth = LocalDate.of(2022, 1, 1)), today)!!.key)
        assertEquals("a0", YearProfiles.of(student(birth = LocalDate.of(2026, 3, 1)), today)!!.key)
        assertEquals("a6", YearProfiles.of(student(birth = LocalDate.of(2019, 5, 1)), LocalDate.of(2026, 2, 27))!!.key) // 3월 입학 직전
        assertEquals("e1", YearProfiles.of(student(birth = LocalDate.of(2019, 5, 1)), LocalDate.of(2026, 3, 2))!!.key)
        assertEquals("e3", YearProfiles.of(student(gradeYear = 9, birth = LocalDate.of(2017, 5, 1)), today)!!.key)
        assertEquals("g", YearProfiles.of(student(birth = LocalDate.of(1990, 1, 1)), today)!!.key)
        assertEquals("m2", YearProfiles.of(student(gradeYear = 8), today)!!.key)
        assertNull(YearProfiles.of(student(), today))
    }

    @Test
    fun kindsAndPlanOptionsCarryTheYearsAmount() {
        val kind = StudyKind("받아쓰기 연습", StudyKindType.WRITE, 2, 10)
        assertEquals("주 2회 · 10분", kind.amountLabel); assertEquals("매일 · 10분", kind.copy(timesPerWeek = 7).amountLabel)
        val h3 = YearProfiles.byKey.getValue("h3").planOptions(PlanOptions(breakMinutes = 5))
        assertEquals(70, h3.sessionMinutes); assertEquals(3, h3.sessionsPerDay); assertTrue(h3.includeWeekend); assertEquals(5, h3.breakMinutes)
        assertFalse(YearProfiles.byKey.getValue("e2").planOptions(PlanOptions()).includeWeekend)
        assertEquals(15, GrowthGuide.defaultPlanOptions(GrowthStage.EARLY_ELEMENTARY, YearProfiles.byKey.getValue("e2")).sessionMinutes)
    }
}
