package com.nextstep.app.domain.time

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SchoolYearTest {
    @Test
    fun schoolYearStartsInMarch() {
        assertEquals(2029, SchoolYear.of(LocalDate.of(2029, 3, 1)))
        assertEquals(2028, SchoolYear.of(LocalDate.of(2029, 2, 28)))
        assertEquals(LocalDate.of(2029, 3, 1), SchoolYear.start(2029))
        assertEquals(LocalDate.of(2032, 2, 29), SchoolYear.end(2031)) // 윤년
    }

    @Test
    fun termsSplitAtSeptember() {
        assertEquals(LocalDate.of(2029, 8, 31), SchoolYear.firstTermEnd(2029))
        assertEquals(LocalDate.of(2029, 9, 1), SchoolYear.secondTermStart(2029))
        assertTrue(SchoolYear.isFirstTerm(LocalDate.of(2029, 8, 31)))
        assertFalse(SchoolYear.isFirstTerm(LocalDate.of(2029, 9, 1)))
        assertFalse(SchoolYear.isFirstTerm(LocalDate.of(2030, 2, 1)))
    }
}
