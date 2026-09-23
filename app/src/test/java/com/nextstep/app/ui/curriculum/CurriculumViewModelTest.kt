package com.nextstep.app.ui.curriculum

import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.fake.FakeContentRepository
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakePeerCurriculumRepository
import com.nextstep.app.fake.FakeSubjectRepository
import com.nextstep.app.fake.FakeTaskRepository
import com.nextstep.app.fake.FakeTopicRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CurriculumViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.STUDENT)
    private val peers = FakePeerCurriculumRepository()
    private val subjects = FakeSubjectRepository(); private val topics = FakeTopicRepository(); private val tasks = FakeTaskRepository(); private val contents = FakeContentRepository()
    private val today = LocalDate.of(2033, 10, 10) // 2020-05-15 생 → 중1 2학기

    private fun vm() = CurriculumViewModel(streams, peers, subjects, topics, tasks, contents, today = { today })

    @Test
    fun birthDatePicksCurrentTermAndNavigationMovesBetweenTerms() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "나", id = "me", birthDate = LocalDate.of(2020, 5, 15)))
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertTrue(s.loaded); assertEquals("g7s2", s.currentPeriodKey); assertEquals("g7s2", s.selectedPeriodKey); assertTrue(s.isCurrent)
        assertEquals("g7s2", s.plan!!.curriculum.periodKey); assertTrue(s.nextPreview.any { it.contains("순환소수") })
        assertEquals(24, s.periods.size)
        vm.onEvent(CurriculumEvent.NextPeriod); s = settle(vm.state)
        assertEquals("g8s1", s.selectedPeriodKey); assertFalse(s.isCurrent)
        vm.onEvent(CurriculumEvent.ThisPeriod); s = settle(vm.state); assertEquals("g7s2", s.selectedPeriodKey)
        repeat(30) { vm.onEvent(CurriculumEvent.PrevPeriod) }; s = settle(vm.state)
        assertEquals("g1s1", s.selectedPeriodKey); assertFalse(s.hasPrev); assertTrue(s.hasNext)
        job.cancel()
    }

    @Test
    fun gradeYearWithoutBirthDateStillGivesATermAndNothingWithNeither() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "나", id = "me", gradeYear = 10))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals("g10s2", s.currentPeriodKey); assertTrue(s.plan!!.curriculum.units.any { it.title.contains("공통수학2") })
        job.cancel()
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "나", id = "me"))
        val vm2 = vm(); val job2 = subscribe(vm2.state)
        val s2 = settle(vm2.state)
        assertTrue(s2.periods.isEmpty()); assertNull(s2.plan); assertNull(s2.currentPeriodKey)
        job2.cancel()
    }

    @Test
    fun importCreatesMissingSubjectAndAddsOnlyUnregisteredUnits() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "나", id = "me", birthDate = LocalDate.of(2020, 5, 15)))
        streams.subjects.value = listOf(Fixtures.math)
        streams.topics.value = listOf(Fixtures.topic("math", "기본 도형", 0))
        val vm = vm(); val job = subscribe(vm.state); settle(vm.state)
        vm.onEvent(CurriculumEvent.ImportSubject("수학")); settle(vm.state)
        assertTrue(subjects.saved.isEmpty())
        val mathCall = topics.calls.single()
        assertTrue(mathCall.startsWith("add:math:")); assertFalse(mathCall.contains("기본 도형")); assertTrue(mathCall.contains("작도와 합동"))
        vm.onEvent(CurriculumEvent.ImportSubject("영어")); settle(vm.state)
        val created = subjects.saved.single()
        assertEquals("영어", created.name); assertTrue(topics.calls.last().startsWith("add:${created.id}:"))
        vm.onEvent(CurriculumEvent.ImportSubject("없는 과목")); settle(vm.state)
        assertEquals(2, topics.calls.size)
        job.cancel()
    }

    @Test
    fun addTaskUsesReviewForUnitsInClassAndPreviewOtherwise() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "나", id = "me", birthDate = LocalDate.of(2020, 5, 15)))
        streams.subjects.value = listOf(Fixtures.math)
        streams.topics.value = listOf(Fixtures.topic("math", "기본 도형", 0, covered = true))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        val units = s.plan!!.subjects.first { it.subject == "수학" }.units
        vm.onEvent(CurriculumEvent.AddTask(units.first { it.unit.title.startsWith("기본 도형") }.unit, "STUDENT"))
        vm.onEvent(CurriculumEvent.AddTask(units.first { it.unit.title == "작도와 합동" }.unit, "STUDENT"))
        vm.onEvent(CurriculumEvent.MarkWatched("c1"))
        settle(vm.state)
        assertEquals(listOf(TaskType.REVIEW, TaskType.PREVIEW), tasks.saved.map { it.type })
        assertEquals("math", tasks.saved.first().subjectId); assertEquals(s.selected!!.end.toEpochDay(), tasks.saved.first().dueDate)
        assertEquals(listOf("c1" to true), contents.watched)
        job.cancel()
    }
}
