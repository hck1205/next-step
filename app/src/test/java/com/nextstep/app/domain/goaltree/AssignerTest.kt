package com.nextstep.app.domain.goaltree

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AssignerTest {
    @Test
    fun roleNamesMapToWhoGaveIt() {
        assertEquals(Assigner.MENTOR, Assigner.of("MENTOR"))
        assertNull(Assigner.of(""))
        assertEquals("학부모가 준 일", Assigner.givenBy("PARENT")?.taskLabel)
        assertNull(Assigner.givenBy("STUDENT")) // 스스로 정한 일은 따로 표시하지 않음
        assertNull(Assigner.givenBy(null))
    }
}
