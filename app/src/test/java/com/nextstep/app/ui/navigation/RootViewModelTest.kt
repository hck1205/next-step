package com.nextstep.app.ui.navigation

import com.nextstep.app.domain.growth.StudentUiLevel
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
    fun switchChildGoesThroughOnboarding() = runTest {
        val vm = RootViewModel(onboarding, members)
        vm.switchChild("famB"); advanceUntilIdle()
        assertEquals(listOf("resume", "switch:famB"), onboarding.calls)
    }

    @Test
    fun studentLevelFollowsMyGradeOrChoiceAndOnlyForStudents() = runTest {
        val vm = RootViewModel(onboarding, members)
        onboarding.profile.value = UserProfile(Role.STUDENT, "지우", "fam", "ABC123", "지우", onboarded = true, memberId = "me")
        members.myMember.value = Fixtures.member(Role.STUDENT, "지우", id = "me", gradeYear = 2)
        advanceUntilIdle()
        assertEquals(StudentUiLevel.SPROUT, vm.state.value!!.studentLevel)
        members.myMember.value = members.myMember.value!!.copy(uiLevel = "STEM")
        advanceUntilIdle()
        assertEquals(StudentUiLevel.STEM, vm.state.value!!.studentLevel)
        onboarding.profile.value = onboarding.profile.value.copy(role = Role.PARENT)
        members.myMember.value = Fixtures.member(Role.PARENT, "엄마", id = "me")
        advanceUntilIdle()
        assertNull(vm.state.value!!.studentLevel)
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
