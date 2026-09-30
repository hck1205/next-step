package com.nextstep.app.domain.family

import com.nextstep.app.data.model.Role
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FamilyLookupsTest {
    @Test
    fun studentSkipsGuardiansAndDeletedRows() {
        val gone = Fixtures.member(Role.STUDENT, "지운 아이").copy(deleted = true)
        val members = listOf(Fixtures.member(Role.PARENT, "엄마"), gone, Fixtures.member(Role.STUDENT, "지우"))
        assertEquals("지우", members.student()?.name)
        assertNull(listOf(Fixtures.member(Role.PARENT, "엄마")).student())
    }

    @Test
    fun subjectById() {
        val subjects = listOf(Fixtures.math, Fixtures.english)
        assertEquals("영어", subjects.byId("eng")?.name)
        assertNull(subjects.byId(null)); assertNull(subjects.byId("art"))
    }
}
