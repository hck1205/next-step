package com.nextstep.app.domain.access

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ParentGateTest {
    @Test
    fun acceptsOnlyTheProduct() {
        val gate = ParentGate(13, 7)
        assertEquals("13 × 7 = ?", gate.question)
        assertTrue(gate.accepts("91")); assertTrue(gate.accepts(" 91 "))
        assertFalse(gate.accepts("90")); assertFalse(gate.accepts("")); assertFalse(gate.accepts("구십일"))
    }

    @Test
    fun questionsAreTwoDigitTimesOneDigit() {
        val random = Random(7)
        repeat(50) {
            val g = ParentGate.next(random)
            assertTrue(g.a in 12..19); assertTrue(g.b in 6..9)
        }
    }
}
