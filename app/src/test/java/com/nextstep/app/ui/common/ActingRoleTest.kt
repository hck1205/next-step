package com.nextstep.app.ui.common

import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.testing.Fixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ActingRoleTest {
    @Test
    fun theWriterIsTheProfileRoleAndAParentMentorWritesAsMentor() = runTest {
        val streams = FakeFamilyDataStreams(role = Role.PARENT)
        assertEquals("PARENT", streams.actingRoleName())
        streams.myMember.value = Fixtures.member(Role.PARENT, "엄마", id = "me", mentorEnabled = true)
        assertEquals("MENTOR", streams.actingRoleName())
        streams.actAs(Role.STUDENT)
        assertEquals("STUDENT", streams.actingRoleName())
    }
}
