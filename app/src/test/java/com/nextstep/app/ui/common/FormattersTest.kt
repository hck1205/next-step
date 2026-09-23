package com.nextstep.app.ui.common

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {
    @Test
    fun ratioIsZeroForEmptyWhole() {
        assertEquals(0f, ratio(3, 0), 0f)
        assertEquals(0.5f, ratio(1, 2), 0f)
    }

    @Test
    fun oneDecimalDropsTrailingZeroAndRoundsToTenth() {
        assertEquals("130", 130.0.oneDecimal()); assertEquals("28.3", 28.25.oneDecimal()); assertEquals("0.7", 0.7.oneDecimal()); assertEquals("-1.5", (-1.5).oneDecimal())
    }

    @Test
    fun percentHelpersTruncate() {
        assertEquals("62%", 0.625f.asPercent()); assertEquals("0%", 0f.asPercent())
        assertEquals(0, ratioPercent(3, 0)); assertEquals(75, ratioPercent(3, 4))
    }
}
