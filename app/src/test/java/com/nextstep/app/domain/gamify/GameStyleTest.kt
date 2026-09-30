package com.nextstep.app.domain.gamify

import com.nextstep.app.domain.growth.StudentUiLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameStyleTest {
    @Test
    fun gameShapeGrowsWithTheScreenStage() {
        assertEquals(
            listOf(GameStyle.STICKERS, GameStyle.STICKERS, GameStyle.LEVELS, GameStyle.LEVELS, GameStyle.GROWTH, GameStyle.GROWTH),
            StudentUiLevel.entries.map { it.game },
        )
        // 어린 나이: 숫자 없는 스티커판, 연속 기록 없음. 청소년: 레벨 이름 없이 Lv, 하루 쉬어도 이어지는 연속 기록, 스스로 끌 수 있음
        assertFalse(GameStyle.STICKERS.showsLevel); assertFalse(GameStyle.STICKERS.showsStreak)
        assertTrue(GameStyle.LEVELS.showsLevelTitle); assertEquals(0, GameStyle.LEVELS.restDays)
        assertFalse(GameStyle.GROWTH.showsLevelTitle); assertEquals(1, GameStyle.GROWTH.restDays); assertTrue(GameStyle.GROWTH.studentCanTurnOff)
        assertTrue(GameStyle.entries.zipWithNext().all { (a, b) -> a.activeDaysTarget <= b.activeDaysTarget })
        GameStyle.entries.forEach { style -> assertTrue(Badge.of(style).size >= 8) }
    }
}
