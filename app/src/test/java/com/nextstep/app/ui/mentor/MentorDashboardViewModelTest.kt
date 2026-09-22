package com.nextstep.app.ui.mentor

import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeMemberRepository
import com.nextstep.app.fake.FakeNoteRepository
import com.nextstep.app.fake.FakeTaskRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class MentorDashboardViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.MENTOR)
    private val members = FakeMemberRepository(); private val tasks = FakeTaskRepository(); private val notes = FakeNoteRepository()
    private val today = DateUtils.today()

    private fun vm() = MentorDashboardViewModel(streams, members, tasks, notes)

    @Test
    fun scopedToAssignedSubjectsAndFlagsSetupWhenNone() = runTest {
        streams.subjects.value = listOf(Fixtures.math, Fixtures.english)
        val me = Fixtures.member(Role.MENTOR, "쌤", id = "me", subjectIds = "math")
        streams.myMember.value = me
        streams.members.value = listOf(me, Fixtures.member(Role.MENTOR, "다른쌤"), Fixtures.member(Role.PARENT, "엄마"))
        streams.sessions.value = listOf(Fixtures.session("math", today, LocalTime.of(9, 0), 30), Fixtures.session("eng", today, LocalTime.of(10, 0), 30))
        streams.grades.value = listOf(Fixtures.grade("math", 80.0, 1), Fixtures.grade("eng", 50.0, 1))
        streams.tasks.value = listOf(Fixtures.task("내 과제", today, "math", by = "MENTOR"), Fixtures.task("영어 과제", today, "eng", by = "MENTOR"), Fixtures.task("학생 것", today, "math"))
        streams.roadmap.value = listOf(Fixtures.roadmap("a", status = RoadmapStatus.IN_PROGRESS, target = today.minusDays(1)), Fixtures.roadmap("b", status = RoadmapStatus.DONE))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(listOf("수학"), s.subjects.map { it.name }); assertFalse(s.needsSubjectSetup)
        assertEquals(listOf("다른쌤"), s.otherMentors.map { it.name })
        assertEquals(30, s.weekMinutes); assertEquals(1, s.scores.size)
        assertEquals(listOf("내 과제"), s.myTasks.map { it.title })
        assertEquals(1, s.roadmapInProgress); assertEquals(1, s.roadmapDone); assertEquals(1, s.roadmapOverdue); assertEquals(2, s.roadmapTotal)

        streams.myMember.value = me.copy(subjectIds = "")
        val all = settle(vm.state)
        assertTrue(all.needsSubjectSetup); assertEquals(2, all.subjects.size); assertEquals(60, all.weekMinutes)
        job.cancel()
    }

    @Test
    fun notesFromOtherMentorsAreHidden() = runTest {
        val me = Fixtures.member(Role.MENTOR, "쌤", id = "me")
        streams.myMember.value = me
        streams.notes.value = listOf(Fixtures.note("내 것", Role.MENTOR, "쌤"), Fixtures.note("남의 것", Role.MENTOR, "다른쌤"), Fixtures.note("부모", Role.PARENT, "엄마"))
        val vm = vm(); val job = subscribe(vm.state)
        assertEquals(listOf("내 것", "부모"), settle(vm.state).notes.map { it.text })
        job.cancel()
    }

    @Test
    fun eventsDelegateAndSetSubjectsNeedsMyMember() = runTest {
        val vm = vm(); val job = subscribe(vm.state); settle(vm.state)
        vm.onEvent(MentorDashboardEvent.SetSubjects(listOf("a")))
        settle(vm.state)
        assertTrue(members.calls.isEmpty())
        streams.myMember.value = Fixtures.member(Role.MENTOR, "쌤", id = "me"); settle(vm.state)
        vm.onEvent(MentorDashboardEvent.SetSubjects(listOf("a")))
        vm.onEvent(MentorDashboardEvent.AssignTask("과제", "math", TaskType.HOMEWORK, today))
        vm.onEvent(MentorDashboardEvent.DeleteTask("t")); vm.onEvent(MentorDashboardEvent.AddNote("피드백")); vm.onEvent(MentorDashboardEvent.DeleteNote("n"))
        settle(vm.state)
        assertEquals(listOf("subjects:me:a"), members.calls)
        assertEquals("MENTOR", tasks.saved.single().createdByRole); assertTrue(tasks.saved.last().deleted || tasks.saved.size == 1)
        assertEquals(listOf("피드백"), notes.added); assertEquals(listOf("n"), notes.deleted)
        job.cancel()
    }
}
