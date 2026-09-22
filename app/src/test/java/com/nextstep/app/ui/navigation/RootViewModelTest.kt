package com.nextstep.app.ui.navigation

import com.nextstep.app.data.model.Role
import com.nextstep.app.data.prefs.UserProfile
import com.nextstep.app.fake.FakeMemberRepository
import com.nextstep.app.fake.FakeOnboardingRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RootViewModelTest : ViewModelTestBase() {
    private val onboarding = FakeOnboardingRepository()
    private val members = FakeMemberRepository()

    @Test
    fun resumesSyncOnStartAndHasNoCapabilitiesBeforeOnboarding() = runTest {
        val vm = RootViewModel(onboarding, members)
        advanceUntilIdle()
        assertEquals(listOf("resume"), onboarding.calls)
        assertNull(vm.state.value!!.capabilities)
    }

    @Test
    fun capabilitiesFollowRoleAndMentorFlag() = runTest {
        val vm = RootViewModel(onboarding, members)
        onboarding.profile.value = UserProfile(Role.PARENT, "엄마", "fam", "ABC123", "학생", onboarded = true, memberId = "me")
        advanceUntilIdle()
        assertFalse(vm.state.value!!.capabilities!!.actsAsMentor)
        members.myMember.value = Fixtures.member(Role.PARENT, "엄마", id = "me", mentorEnabled = true)
        advanceUntilIdle()
        assertTrue(vm.state.value!!.capabilities!!.actsAsMentor)
        onboarding.profile.value = onboarding.profile.value.copy(role = Role.MENTOR)
        members.myMember.value = null
        advanceUntilIdle()
        assertTrue(vm.state.value!!.capabilities!!.actsAsMentor)
    }
}
