package com.nextstep.app.domain.insight

import com.nextstep.app.data.model.ActivityType
import com.nextstep.app.data.model.AptitudeDomain
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AptitudeEngineTest {
    private val today = LocalDate.of(2029, 10, 10)

    @Test
    fun domainIsGuessedFromTitleThenType() {
        assertEquals(AptitudeDomain.MUSIC, AptitudeEngine.domainOf(Fixtures.activity("피아노 학원", ActivityType.HOBBY)))
        assertEquals(AptitudeDomain.SPORT, AptitudeEngine.domainOf(Fixtures.activity("주말 축구 클럽", ActivityType.CLUB)))
        assertEquals(AptitudeDomain.SOCIAL, AptitudeEngine.domainOf(Fixtures.activity("노인정 방문", ActivityType.VOLUNTEER)))
        assertNull(AptitudeEngine.domainOf(Fixtures.activity("할머니 댁", ActivityType.TRAVEL)))
        assertEquals(AptitudeDomain.ART, AptitudeDomain.guessFrom("드로잉 수업")); assertNull(AptitudeDomain.guessFrom(""))
    }

    @Test
    fun nothingBelowThresholdAndAtMostThreeSignals() {
        assertTrue(AptitudeEngine.signals(emptyList(), emptyList(), today).isEmpty())
        val one = listOf(Fixtures.activity("미술 체험", ActivityType.EXPERIENCE, today))
        assertTrue(AptitudeEngine.signals(one, emptyList(), today).isEmpty()) // 활동 1개 = 1점
        val many = listOf(
            Fixtures.activity("피아노", ActivityType.HOBBY, today.minusMonths(1), id = "1"), Fixtures.activity("합창", ActivityType.CLUB, today, id = "2"), Fixtures.activity("음악회", ActivityType.EXPERIENCE, today, id = "3"),
            Fixtures.activity("축구", ActivityType.HOBBY, today, id = "4"), Fixtures.activity("수영", ActivityType.HOBBY, today, id = "5"), Fixtures.activity("농구", ActivityType.CLUB, today, id = "6"),
            Fixtures.activity("그림", ActivityType.HOBBY, today, id = "7"), Fixtures.activity("미술관", ActivityType.FIELD_TRIP, today, id = "8"), Fixtures.activity("만들기", ActivityType.EXPERIENCE, today, id = "9"),
            Fixtures.activity("코딩", ActivityType.HOBBY, today, id = "10"), Fixtures.activity("로봇", ActivityType.CLUB, today, id = "11"), Fixtures.activity("과학 실험", ActivityType.EXPERIENCE, today, id = "12"),
        )
        assertEquals(3, AptitudeEngine.signals(many, emptyList(), today).size)
    }

    @Test
    fun scoreCombinesObservationsActivitiesPersistenceAndRatingsWithEvidence() {
        val activities = listOf(
            Fixtures.activity("피아노 학원", ActivityType.HOBBY, today.minusMonths(14), rating = 5, id = "p"),
            Fixtures.activity("학교 합창단 발표", ActivityType.COMPETITION, today.minusMonths(2), rating = 4, id = "c"),
            Fixtures.activity("축구 체험", ActivityType.EXPERIENCE, today, id = "s"),
        )
        val observations = listOf(Fixtures.observation(AptitudeDomain.MUSIC, "한 번 듣고 따라 부른다", 3), Fixtures.observation(AptitudeDomain.SPORT, "공 다루기가 자연스럽다", 1, id = "o2"))
        val signals = AptitudeEngine.signals(activities, observations, today)
        val music = signals.first()
        assertEquals(AptitudeDomain.MUSIC, music.domain)
        // 관찰 3×2 + 활동 2 + 14개월/3 = 4 + 별점 4 이상 2 = 14 → 3단계
        assertEquals(14, music.score); assertEquals(3, music.stage)
        assertTrue(music.evidence.any { it.contains("관찰 1회") && it.contains("남들이 먼저") })
        assertTrue(music.evidence.any { it.contains("피아노 학원 14개월째") })
        assertTrue(music.evidence.any { it.contains("별점 4 이상 준 활동 2개") })
        assertTrue(music.nextStep.contains("무대"))
        val sport = signals.first { it.domain == AptitudeDomain.SPORT }
        assertEquals(3, sport.score); assertEquals(1, sport.stage); assertTrue(sport.nextStep.contains("체험"))
        assertTrue(signals.none { it.domain == AptitudeDomain.ART })
    }

    @Test
    fun deletedRowsAreIgnored() {
        val activities = listOf(Fixtures.activity("피아노", ActivityType.HOBBY, today.minusMonths(20), rating = 5).copy(deleted = true))
        val observations = listOf(Fixtures.observation(AptitudeDomain.MUSIC, "x", 3).copy(deleted = true))
        assertTrue(AptitudeEngine.signals(activities, observations, today).isEmpty())
    }
}
