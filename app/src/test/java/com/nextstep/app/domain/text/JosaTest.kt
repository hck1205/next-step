package com.nextstep.app.domain.text

import org.junit.Assert.assertEquals
import org.junit.Test

class JosaTest {
    @Test
    fun particlesFollowTheFinalConsonant() {
        assertEquals("수학을", Josa.withObject("수학")); assertEquals("영어를", Josa.withObject("영어"))
        assertEquals("과학은", Josa.withTopic("과학")); assertEquals("국어는", Josa.withTopic("국어"))
        assertEquals("한국사가", Josa.withSubject("한국사")); assertEquals("독서가", Josa.withSubject("독서"))
        assertEquals("ABC를", Josa.withObject("ABC"))
    }
}
