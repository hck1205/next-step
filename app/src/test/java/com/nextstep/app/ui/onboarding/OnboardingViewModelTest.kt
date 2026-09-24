package com.nextstep.app.ui.onboarding

import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.FakeOnboardingRepository
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingViewModelTest : ViewModelTestBase() {
    private val repo = FakeOnboardingRepository(syncAvailable = false)
    private fun vm() = OnboardingViewModel(repo)

    @Test
    fun roleSelectionAdvancesAndBackReturns() {
        val vm = vm()
        vm.onEvent(OnboardingEvent.SelectRole(Role.MENTOR))
        assertEquals(1, vm.state.value.step); assertEquals(Role.MENTOR, vm.state.value.role); assertFalse(vm.state.value.syncAvailable)
        vm.onEvent(OnboardingEvent.Back)
        assertEquals(0, vm.state.value.step)
        vm.onEvent(OnboardingEvent.Back)
        assertEquals(0, vm.state.value.step)
    }

    @Test
    fun codeIsUppercasedAndCappedAtSix() {
        val vm = vm()
        vm.onEvent(OnboardingEvent.SetCode("abc123xyz"))
        assertEquals("ABC123", vm.state.value.code)
    }

    @Test
    fun submitValidatesNameAndCodeBeforeCallingRepository() = runTest {
        val vm = vm()
        vm.onEvent(OnboardingEvent.SelectRole(Role.PARENT))
        vm.onEvent(OnboardingEvent.Submit)
        assertEquals("이름을 입력해 주세요", vm.state.value.error)
        vm.onEvent(OnboardingEvent.SetName("엄마"))
        vm.onEvent(OnboardingEvent.Submit)
        assertTrue(vm.state.value.error!!.contains("6자리"))
        assertTrue(repo.calls.isEmpty())
        vm.onEvent(OnboardingEvent.SetCode("ABC123"))
        vm.onEvent(OnboardingEvent.SetTitle(" 담임 "))
        vm.onEvent(OnboardingEvent.Submit)
        advanceUntilIdle()
        assertEquals(listOf("join:PARENT:엄마:ABC123:담임"), repo.calls)
        assertNull(vm.state.value.error)
    }

    @Test
    fun studentSubmitCreatesFamilyAndFailureSurfacesMessage() = runTest {
        val vm = vm()
        vm.onEvent(OnboardingEvent.SelectRole(Role.STUDENT)); vm.onEvent(OnboardingEvent.SetName("민수"))
        vm.onEvent(OnboardingEvent.Submit)
        advanceUntilIdle()
        assertEquals(listOf("create:민수:0"), repo.calls)
        vm.onEvent(OnboardingEvent.SetGrade(8)); vm.onEvent(OnboardingEvent.Submit); advanceUntilIdle()
        assertEquals("create:민수:8", repo.calls.last())
        repo.createResult = Result.failure(IllegalStateException("서버 오류"))
        vm.onEvent(OnboardingEvent.Submit)
        advanceUntilIdle()
        assertEquals("서버 오류", vm.state.value.error); assertFalse(vm.state.value.loading)
        assertNotNull(vm.state.value.role)
    }

    @Test
    fun studentBirthDateIsPassedThrough() = runTest {
        val vm = vm()
        vm.onEvent(OnboardingEvent.SelectRole(Role.STUDENT)); vm.onEvent(OnboardingEvent.SetName("민수"))
        vm.onEvent(OnboardingEvent.SetBirthDate(java.time.LocalDate.of(2012, 4, 3))); vm.onEvent(OnboardingEvent.Submit)
        advanceUntilIdle()
        assertEquals(listOf("create:민수:0:2012-04-03"), repo.calls)
    }

    @Test
    fun parentCanStartFamilyForChildWithoutCode() = runTest {
        val vm = vm()
        vm.onEvent(OnboardingEvent.SelectRole(Role.PARENT)); vm.onEvent(OnboardingEvent.SetName("엄마"))
        vm.onEvent(OnboardingEvent.SetCreateAsParent(true))
        vm.onEvent(OnboardingEvent.Submit)
        assertEquals("자녀 이름을 입력해 주세요", vm.state.value.error); assertTrue(repo.calls.isEmpty())
        vm.onEvent(OnboardingEvent.SetChildName(" 아기 ")); vm.onEvent(OnboardingEvent.SetBirthDate(java.time.LocalDate.of(2026, 7, 1)))
        vm.onEvent(OnboardingEvent.Submit)
        advanceUntilIdle()
        assertEquals(listOf("createAsParent:엄마:아기:2026-07-01"), repo.calls)
        repo.calls.clear(); vm.onEvent(OnboardingEvent.SetRelation(com.nextstep.app.data.model.GuardianRelation.GRANDMA)); vm.onEvent(OnboardingEvent.Submit); advanceUntilIdle()
        assertEquals(listOf("createAsParent:엄마:아기:2026-07-01:할머니"), repo.calls); repo.calls.clear()
        // 토글을 끄면 다시 코드가 필요
        vm.onEvent(OnboardingEvent.SetCreateAsParent(false)); vm.onEvent(OnboardingEvent.Submit)
        assertTrue(vm.state.value.error!!.contains("6자리"))
    }
}
