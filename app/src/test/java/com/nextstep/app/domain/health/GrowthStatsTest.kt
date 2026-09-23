package com.nextstep.app.domain.health

import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GrowthStatsTest {
    private val today = LocalDate.of(2029, 10, 10)

    @Test
    fun emptyOrValuelessRecordsGiveEmptySummary() {
        val s = GrowthStats.summarize(listOf(Fixtures.growth(today)), today)
        assertFalse(s.hasAny); assertNull(s.heightCm); assertTrue(s.signals.isEmpty())
        assertFalse(GrowthStats.summarize(emptyList(), today).hasAny)
    }

    @Test
    fun latestValuesComeFromTheNewestRecordThatHasThemAndBmiIsComputed() {
        val records = listOf(
            Fixtures.growth(LocalDate.of(2029, 9, 1), height = 130.0, weight = 27.0, visionL = 1.0, visionR = 1.0, id = "a"),
            Fixtures.growth(LocalDate.of(2029, 3, 1), height = 127.0, weight = 26.0, id = "b"),
            Fixtures.growth(LocalDate.of(2029, 10, 1), weight = 28.0, id = "c"),
            Fixtures.growth(LocalDate.of(2029, 10, 5), height = 200.0, id = "gone").copy(deleted = true),
        )
        val s = GrowthStats.summarize(records, today)
        assertTrue(s.hasAny); assertEquals(LocalDate.of(2029, 10, 1), s.latestDate)
        assertEquals(130.0, s.heightCm!!, 0.0); assertEquals(28.0, s.weightKg!!, 0.0); assertEquals(1.0, s.visionLeft!!, 0.0)
        assertEquals(28.0 / (1.3 * 1.3), s.bmi!!, 0.001); assertEquals(1.0, s.weightDeltaKg!!, 0.0001)
        // 3/1 → 9/1 : 3cm / 184일 × 365.25 ≈ 5.95cm/년 → 느림 신호 없음
        assertEquals(3.0 / 184 * 365.25, s.heightVelocityCmPerYear!!, 0.001)
        assertTrue(s.signals.none { it.title.contains("느린") })
    }

    @Test
    fun velocityNeedsTwoHeightsAtLeastTwoMonthsApart() {
        assertNull(GrowthStats.velocity(listOf(Fixtures.growth(today, height = 130.0))))
        assertNull(GrowthStats.velocity(listOf(Fixtures.growth(today.minusDays(30), height = 129.0, id = "a"), Fixtures.growth(today, height = 130.0, id = "b"))))
        val slow = GrowthStats.summarize(listOf(Fixtures.growth(today.minusDays(365), height = 129.0, id = "a"), Fixtures.growth(today, height = 131.0, id = "b")), today)
        assertTrue(slow.signals.any { it.title.contains("느린") && it.level == GrowthSignalLevel.CHECK })
    }

    @Test
    fun visionSignalsFireOnLowOrDroppingVision() {
        val low = GrowthStats.summarize(listOf(Fixtures.growth(today, visionL = 0.7, visionR = 1.0)), today)
        assertTrue(low.signals.any { it.title.contains("시력 검진") })
        val drop = GrowthStats.summarize(listOf(Fixtures.growth(today.minusDays(200), visionL = 1.2, visionR = 1.2, id = "a"), Fixtures.growth(today, visionL = 0.9, visionR = 1.0, id = "b")), today)
        assertTrue(drop.signals.any { it.title.contains("빠르게") })
        val fine = GrowthStats.summarize(listOf(Fixtures.growth(today.minusDays(200), visionL = 1.0, visionR = 1.0, id = "a"), Fixtures.growth(today, visionL = 0.9, visionR = 1.0, id = "b")), today)
        assertTrue(fine.signals.none { it.level == GrowthSignalLevel.CHECK })
    }

    @Test
    fun staleRecordsGetAnInfoReminder() {
        val stale = GrowthStats.summarize(listOf(Fixtures.growth(today.minusMonths(7), height = 120.0)), today)
        assertTrue(stale.signals.any { it.level == GrowthSignalLevel.INFO && it.title.contains("6개월") })
        val fresh = GrowthStats.summarize(listOf(Fixtures.growth(today.minusMonths(2), height = 120.0)), today)
        assertTrue(fresh.signals.isEmpty())
    }
}
