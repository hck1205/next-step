package com.nextstep.app.domain.text

import org.junit.Assert.assertEquals
import org.junit.Test

class NumberTextTest {
    @Test
    fun wholeNumbersDropDecimalsOthersKeepOne() {
        assertEquals("85", 85.0.compact())
        assertEquals("85.5", 85.5.compact())
        assertEquals("5.9", 5.94.compact())
        assertEquals("0", 0.0.compact())
        assertEquals("-3.2", (-3.2).compact())
    }
}
