package com.nextstep.app.domain.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RatioTest {
    @Test
    fun ratiosGuardAnEmptyWhole() {
        assertEquals(0.25f, ratioOf(1, 4), 0.0001f); assertEquals(0f, ratioOf(3, 0), 0f)
        assertEquals(0.5f, ratioOrNull(2, 4)!!, 0.0001f); assertNull(ratioOrNull(1, 0))
    }

    @Test
    fun percentsRoundDown() {
        assertEquals(66, percentOf(2, 3)); assertEquals(0, percentOf(1, 0))
        assertEquals(33, (1f / 3).toPercent()); assertEquals(100, 1f.toPercent())
    }
}
