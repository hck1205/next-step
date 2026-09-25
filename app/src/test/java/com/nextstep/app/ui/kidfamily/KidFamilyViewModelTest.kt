package com.nextstep.app.ui.kidfamily

import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class KidFamilyViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.STUDENT)
    private fun vm() = KidFamilyViewModel(streams, Random(3))

    @Test
    fun familyWithoutMe() = runTest {
        val me = Fixtures.member(Role.STUDENT, "지우", id = "me")
        streams.members.value = listOf(me, Fixtures.member(Role.PARENT, "엄마", id = "mom"), Fixtures.member(Role.MENTOR, "쌤", id = "t"))
        streams.myMember.value = me
        val vm = vm(); val job = subscribe(vm.state)
        assertEquals(listOf("mom", "t"), settle(vm.state).family.map { it.id })
        job.cancel()
    }

    @Test
    fun settingsOpenOnlyAfterTheGrownUpAnswersTheGate() = runTest {
        val vm = vm(); val job = subscribe(vm.state)
        vm.onEvent(KidFamilyEvent.OpenSettings)
        val first = settle(vm.state).gate!!
        vm.onEvent(KidFamilyEvent.AnswerGate("1"))
        var s = settle(vm.state)
        assertTrue(s.gateError); assertFalse(s.unlocked); assertNotNull(s.gate)
        vm.onEvent(KidFamilyEvent.AnswerGate((s.gate!!.a * s.gate!!.b).toString()))
        s = settle(vm.state)
        assertTrue(s.unlocked); assertNull(s.gate); assertFalse(s.gateError)
        vm.onEvent(KidFamilyEvent.ConsumeUnlock); assertFalse(settle(vm.state).unlocked)
        vm.onEvent(KidFamilyEvent.OpenSettings); vm.onEvent(KidFamilyEvent.CloseGate); assertNull(settle(vm.state).gate)
        assertTrue(first.a in 12..19)
        job.cancel()
    }
}
