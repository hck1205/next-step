package com.nextstep.app.domain.stats

import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RoadmapStatsTest {
    private val today = LocalDate.of(2029, 10, 1)

    @Test
    fun emptyListGivesEmptySummary() {
        val s = RoadmapStats.summarize(emptyList(), today)
        assertTrue(s.isEmpty); assertEquals(RoadmapSummary(), s)
    }

    @Test
    fun countsStatusesAndOverdueOnlyForUnfinished() {
        val items = listOf(
            Fixtures.roadmap("a", status = RoadmapStatus.IN_PROGRESS, target = today.minusDays(1)),
            Fixtures.roadmap("b", status = RoadmapStatus.DONE, target = today.minusDays(5)),
            Fixtures.roadmap("c", status = RoadmapStatus.PLANNED, target = today),
            Fixtures.roadmap("d", status = RoadmapStatus.PLANNED),
        )
        val s = RoadmapStats.summarize(items, today)
        assertEquals(RoadmapSummary(total = 4, inProgress = 1, done = 1, overdue = 1), s)
    }
}
