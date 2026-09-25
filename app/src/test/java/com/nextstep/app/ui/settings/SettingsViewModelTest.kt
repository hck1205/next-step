package com.nextstep.app.ui.settings

import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeMemberRepository
import com.nextstep.app.fake.FakeOnboardingRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val onboarding = FakeOnboardingRepository(syncAvailable = true)
    private val members = FakeMemberRepository()

    private fun vm() = SettingsViewModel(streams, onboarding, members)

    @Test
    fun stateCombinesProfileMembersAndSync() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "나", id = "me"), Fixtures.member(Role.MENTOR, "쌤"))
        streams.myMember.value = streams.members.value.first()
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(Role.PARENT, s.profile!!.role); assertTrue(s.syncAvailable); assertEquals(2, s.members.size); assertEquals("나", s.me!!.name)
        job.cancel()
    }

    @Test
    fun memberEventsRequireMyMemberExceptRemove() = runTest {
        val vm = vm(); val job = subscribe(vm.state); settle(vm.state)
        vm.onEvent(SettingsEvent.SetMySubjects(listOf("a")))
        vm.onEvent(SettingsEvent.SetMentorEnabled(true))
        vm.onEvent(SettingsEvent.UpdateMyProfile("n", "t"))
        vm.onEvent(SettingsEvent.RemoveMember("other"))
        settle(vm.state)
        assertEquals(listOf("remove:other"), members.calls)

        streams.myMember.value = Fixtures.member(Role.PARENT, "엄마", id = "me")
        settle(vm.state)
        vm.onEvent(SettingsEvent.SetMySubjects(listOf("a", "b")))
        vm.onEvent(SettingsEvent.SetMentorEnabled(true))
        vm.onEvent(SettingsEvent.UpdateMyProfile("엄마2", "담임"))
        settle(vm.state)
        assertEquals(listOf("remove:other", "subjects:me:a|b", "mentor:me:true", "profile:me:엄마2:담임"), members.calls)
        job.cancel()
    }

    @Test
    fun childrenComeFromProfileAndChildEventsGoToOnboarding() = runTest {
        streams.profile.value = streams.profile.value.copy(children = listOf(com.nextstep.app.data.prefs.LinkedChild("fam", "지우", "A", "me"), com.nextstep.app.data.prefs.LinkedChild("famB", "하은", "B", "me2")))
        onboarding.joinResult = Result.failure(IllegalArgumentException("코드에 해당하는 학생을 찾지 못했습니다"))
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertEquals(listOf("지우", "하은"), s.children.map { it.studentName }); assertEquals("fam", s.activeFamilyId)
        vm.onEvent(SettingsEvent.SwitchChild("famB")); vm.onEvent(SettingsEvent.AddChild(" 막내 ", null)); vm.onEvent(SettingsEvent.AddChild(" ", null)); vm.onEvent(SettingsEvent.LinkChild("zzz999"))
        s = settle(vm.state)
        assertEquals(listOf("switch:famB", "addChild:막내:null", "linkChild:zzz999"), onboarding.calls)
        assertEquals("코드에 해당하는 학생을 찾지 못했습니다", s.childError)
        vm.onEvent(SettingsEvent.DismissChildError)
        assertEquals(null, settle(vm.state).childError)
        job.cancel()
    }

    @Test
    fun signOutAndSyncGoToOnboardingRepository() = runTest {
        val vm = vm(); val job = subscribe(vm.state)
        vm.onEvent(SettingsEvent.RequestSync); vm.onEvent(SettingsEvent.SignOut)
        settle(vm.state)
        assertEquals(listOf("requestSync", "signOut"), onboarding.calls)
        job.cancel()
    }

    @Test
    fun setGradeYearTargetsStudentMember() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.PARENT, "엄마", id = "me"), Fixtures.member(Role.STUDENT, "나", id = "kid"))
        val vm = vm(); val job = subscribe(vm.state); settle(vm.state)
        vm.onEvent(SettingsEvent.SetGradeYear(5))
        vm.onEvent(SettingsEvent.SetBirthDate(java.time.LocalDate.of(2015, 2, 1)))
        settle(vm.state)
        assertEquals(listOf("grade:kid:5", "birth:kid:2015-02-01"), members.calls)
        job.cancel()
    }

    @Test
    fun studentScreenShowsAutoAndChosenLevelAndTargetsStudent() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.PARENT, "엄마", id = "me"), Fixtures.member(Role.STUDENT, "나", id = "kid", gradeYear = 4).copy(uiLevel = "SPROUT"))
        val vm = vm(); val job = subscribe(vm.state); val s = settle(vm.state)
        assertEquals(StudentUiLevel.SEEDLING, s.autoStudentLevel); assertEquals(StudentUiLevel.SPROUT, s.chosenStudentLevel)
        vm.onEvent(SettingsEvent.SetStudentLevel(null)); vm.onEvent(SettingsEvent.SetStudentLevel(StudentUiLevel.STEM)); settle(vm.state)
        assertEquals(listOf("uiLevel:kid:null", "uiLevel:kid:STEM"), members.calls)
        job.cancel()
    }

    @Test
    fun yearLabelAndSaveStudentYearWritesOnlyChangedFields() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.PARENT, "엄마", id = "me"), Fixtures.member(Role.STUDENT, "나", id = "kid", gradeYear = 4))
        val vm = vm(); val job = subscribe(vm.state); val s = settle(vm.state)
        assertEquals("초4", s.yearLabel)
        vm.onEvent(SettingsEvent.SaveStudentYear(birthDate = null, gradeYear = 4, level = null)); settle(vm.state)
        assertEquals(emptyList<String>(), members.calls)
        vm.onEvent(SettingsEvent.SaveStudentYear(birthDate = null, gradeYear = 5, level = StudentUiLevel.STEM)); settle(vm.state)
        assertEquals(listOf("grade:kid:5", "uiLevel:kid:STEM"), members.calls)
        job.cancel()
    }
}
