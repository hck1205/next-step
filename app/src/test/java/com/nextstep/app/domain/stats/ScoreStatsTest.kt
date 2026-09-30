package com.nextstep.app.domain.stats

import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScoreStatsTest {
    @Test
    fun averagePercentOfRecordsOrNull() {
        assertNull(ScoreStats.averagePercent(emptyList()))
        assertEquals(75.0, ScoreStats.averagePercent(listOf(Fixtures.grade("math", 80.0, 1), Fixtures.grade("eng", 70.0, 1)))!!, 0.001)
    }
}
