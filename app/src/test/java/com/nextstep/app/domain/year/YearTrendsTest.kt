package com.nextstep.app.domain.year

import com.nextstep.app.domain.growth.YearProfiles
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YearTrendsTest {
    @Test
    fun everyYearUntilHighSchoolHasATrendAndAdvice() {
        YearProfiles.all.map { it.key }.filter { it != "u" && it != "g" }.forEach { key ->
            val t = YearTrends.of(key)
            assertTrue(key, t != null && t.common.isNotBlank() && t.advice.isNotBlank())
        }
        assertNull(YearTrends.of("u"))
        assertNull(YearTrends.of("g"))
    }

    @Test
    fun earlyYearsWarnAgainstTestsAndScreens() {
        assertTrue(YearTrends.of("a0")!!.advice.contains("0분"))
        assertTrue(YearTrends.of("a4")!!.advice.contains("레벨테스트"))
        assertTrue(YearTrends.of("a6")!!.advice.contains("초1 국어"))
    }
}
