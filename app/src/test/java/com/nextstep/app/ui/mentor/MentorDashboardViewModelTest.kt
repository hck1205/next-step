package com.nextstep.app.ui.mentor

import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.domain.stats.Submissions
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.domain.today.MentorTodayCard
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeMemberRepository
import com.nextstep.app.fake.FakeTaskRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import java.time.LocalTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MentorDashboardViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.MENTOR)
    private val members = FakeMemberRepository(); private val tasks = FakeTaskRepository()
    private val today = DateUtils.today()

    private fun vm() = MentorDashboardViewModel(streams, members, tasks)

    @Test
    fun scopedToAssignedSubjectsAndFlagsSetupWhenNone() = runTest {
        streams.subjects.value = listOf(Fixtures.math, Fixtures.english)
        val me = Fixtures.member(Role.MENTOR, "쌤", id = "me", subjectIds = "math")
        streams.myMember.value = me
        streams.members.value = listOf(me, Fixtures.member(Role.MENTOR, "다른쌤"), Fixtures.member(Role.PARENT, "엄마"))
        // 학생 구성원이 없으면 단계도 없다
        streams.sessions.value = listOf(Fixtures.session("math", today, LocalTime.of(9, 0), 30), Fixtures.session("eng", today, LocalTime.of(10, 0), 30))
        streams.grades.value = listOf(Fixtures.grade("math", 80.0, 1), Fixtures.grade("eng", 50.0, 1))
        streams.tasks.value = listOf(Fixtures.task("내 과제", today, "math", by = "MENTOR"), Fixtures.task("영어 과제", today, "eng", by = "MENTOR"), Fixtures.task("학생 것", today, "math"))
        streams.roadmap.value = listOf(Fixtures.roadmap("a", status = RoadmapStatus.IN_PROGRESS, target = today.minusDays(1)), Fixtures.roadmap("b", status = RoadmapStatus.DONE))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(listOf("수학"), s.subjects.map { it.name }); assertFalse(s.needsSubjectSetup)
        assertEquals(listOf("다른쌤"), s.otherMentors.map { it.name })
        assertNull(s.stage)
        assertEquals(30, s.trends.recent); assertEquals(1, s.trends.scores.size)
        assertEquals(listOf("내 과제"), s.myTasks.map { it.title })
        assertEquals(1, s.roadmap.inProgress); assertEquals(1, s.roadmap.done); assertEquals(1, s.roadmap.overdue); assertEquals(2, s.roadmap.total)
        assertEquals(80, s.trends.scoreAverage)
        // 차트 값도 담당 과목으로 좁혀짐: 수학 공부 30분, 수학 과제 하나(기한 전)
        assertEquals(30, s.trends.recent); assertEquals(Submissions(done = 0, pending = 1, late = 0), s.trends.submissions)
        assertTrue(MentorTodayCard.SUBMISSIONS in s.visibleCards); assertTrue(MentorTodayCard.STUDY_WEEKS in s.visibleCards)

        streams.myMember.value = me.copy(subjectIds = "")
        val all = settle(vm.state)
        assertTrue(all.needsSubjectSetup); assertEquals(2, all.subjects.size); assertEquals(60, all.trends.recent)
        job.cancel()
    }

    @Test
    fun weeklyFeedbackSpeaksOnlyAboutMySubjects() = runTest {
        streams.subjects.value = listOf(Fixtures.math, Fixtures.english)
        streams.myMember.value = Fixtures.member(Role.MENTOR, "쌤", id = "me", subjectIds = "math")
        // 수학은 지난주 닷새 하고 이번 주엔 없음, 영어는 이번 주 닷새(멘토 범위 밖)
        streams.sessions.value = (8L..12L).map { Fixtures.session("math", today.minusDays(it), LocalTime.of(9, 0), 30) } +
            (0L..4L).map { Fixtures.session("eng", today.minusDays(it), LocalTime.of(10, 0), 30) }
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertTrue(s.feedback.isNotEmpty()); assertTrue(s.feedback.none { it.title.contains("영어") })
        assertTrue(s.feedback.any { it.title.startsWith("수학") }); assertTrue(MentorTodayCard.FEEDBACK in s.visibleCards)
        // 학부모에게 보낼 수업 리포트: 같은 사실을 학부모의 말로, 담당 과목만
        val report = s.report!!
        assertTrue(report.title.contains("수학")); assertTrue(MentorTodayCard.REPORT in s.visibleCards)
        val notes = report.sections.first { it.label == "이번 주 살펴본 것" }.lines
        assertTrue(notes.any { it.startsWith("공부한 날이 줄었어요") }); assertTrue(report.sections.flatMap { it.lines }.none { it.contains("영어") })
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
        vm.onEvent(MentorDashboardEvent.DeleteTask("t"))
        settle(vm.state)
        assertEquals(listOf("subjects:me:a"), members.calls)
        assertEquals("MENTOR", tasks.saved.single().createdByRole); assertTrue(tasks.saved.last().deleted || tasks.saved.size == 1)
        job.cancel()
    }

    @Test
    fun todayCardsGroupByConcernAndHideEmptyOnes() = runTest {
        streams.subjects.value = listOf(Fixtures.math)
        streams.myMember.value = Fixtures.member(Role.MENTOR, "쌤", id = "me", subjectIds = "math")
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertFalse(MentorTodayCard.GRADES in s.visibleCards) // 성적이 없으면 카드도 없음
        assertEquals(Concern.OVERVIEW, s.todayGroups.first().concern)
        streams.grades.value = listOf(Fixtures.grade("math", 80.0, 1))
        s = settle(vm.state)
        assertEquals(listOf(MentorTodayCard.SCORES, MentorTodayCard.GRADES), s.todayGroups.single { it.concern == Concern.EXAMS }.cards)
        assertEquals(listOf(80), s.trends.scores.single().percents)
        assertFalse(MentorTodayCard.SUBMISSIONS in s.visibleCards) // 낸 과제가 없으면 제출 카드도 없음
        job.cancel()
    }
}
