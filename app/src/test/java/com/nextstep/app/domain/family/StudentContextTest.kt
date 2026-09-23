package com.nextstep.app.domain.family

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StudentContextTest {
    private val today = LocalDate.of(2029, 10, 10)

    @Test
    fun birthDateGivesCalendarStageAndCurrentPeriod() {
        val members = listOf(Fixtures.member(Role.PARENT, "엄마"), Fixtures.member(Role.STUDENT, "아이", id = "kid", birthDate = LocalDate.of(2020, 5, 15), gradeYear = 3))
        val ctx = StudentContext.of(members, today)
        assertEquals("kid", ctx.student!!.id); assertTrue(ctx.hasBirthDate)
        assertEquals(GrowthStage.EARLY_ELEMENTARY, ctx.stage); assertEquals("g3s2", ctx.currentPeriodKey); assertEquals("초3", ctx.gradeLabel)
        assertTrue(ctx.periods.isNotEmpty())
    }

    @Test
    fun gradeOnlyOrNoStudentDegradesGracefully() {
        val gradeOnly = StudentContext.of(listOf(Fixtures.member(Role.STUDENT, "아이", gradeYear = 8)), today)
        assertFalse(gradeOnly.hasBirthDate); assertEquals(GrowthStage.MIDDLE, gradeOnly.stage); assertTrue(gradeOnly.periods.isEmpty()); assertNull(gradeOnly.currentPeriod); assertEquals("중2", gradeOnly.gradeLabel)
        val none = StudentContext.of(listOf(Fixtures.member(Role.PARENT, "엄마")), today)
        assertNull(none.student); assertNull(none.stage); assertNull(none.currentPeriodKey); assertNull(none.gradeLabel)
    }
}
