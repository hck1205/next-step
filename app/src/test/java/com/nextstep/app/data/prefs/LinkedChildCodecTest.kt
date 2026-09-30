package com.nextstep.app.data.prefs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkedChildCodecTest {
    private val a = LinkedChild("famA", "지우", "ABC123", "m1")
    private val b = LinkedChild("famB", "하은\t둘째\n", "XYZ789", "m2")

    @Test
    fun roundTripsAndCleansSeparators() {
        val decoded = LinkedChildCodec.decode(LinkedChildCodec.encode(listOf(a, b)))
        assertEquals(listOf(a, b.copy(studentName = "하은 둘째 ")), decoded)
    }

    @Test
    fun emptyOrBrokenInputGivesNoChildren() {
        assertTrue(LinkedChildCodec.decode(null).isEmpty())
        assertTrue(LinkedChildCodec.decode("").isEmpty())
        assertTrue(LinkedChildCodec.decode("only\ttwo").isEmpty())
    }

    @Test
    fun upsertReplacesInPlaceOrAppends() {
        val list = LinkedChildCodec.upsert(listOf(a, b), a.copy(studentName = "지우2"))
        assertEquals(listOf("지우2", b.studentName), list.map { it.studentName })
        assertEquals(3, LinkedChildCodec.upsert(list, LinkedChild("famC", "막내", "Q", "m3")).size)
    }
}
