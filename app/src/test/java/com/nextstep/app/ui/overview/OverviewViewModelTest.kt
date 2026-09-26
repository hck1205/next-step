package com.nextstep.app.ui.overview

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.domain.project.ProjectCatalog
import com.nextstep.app.domain.project.ProjectPlanner
import com.nextstep.app.domain.stats.BalanceVerdict
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class OverviewViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val today = LocalDate.of(2029, 10, 10)

    @Test
    fun balanceUsesStageAndCurrentPeriodFromBirthDate() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "아이", id = "kid", birthDate = LocalDate.of(2020, 5, 15)))
        streams.activities.value = listOf(Fixtures.activity("과학관", date = LocalDate.of(2029, 10, 1)))
        streams.tasks.value = listOf(Fixtures.task("a", today, by = "STUDENT"))
        val vm = OverviewViewModel(streams, today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertTrue(s.loaded); assertEquals(GrowthStage.EARLY_ELEMENTARY, s.stage); assertEquals("초3 2학기", s.currentPeriodLabel); assertEquals("초3", s.yearLabel)
        val b = s.balance!!
        assertEquals(1, b.experiencesThisPeriod); assertEquals(1f, b.selfDirectedRatio!!, 0f); assertEquals(200, b.recommendedWeekMinutes) // 초3 올해 프로필: 하루 40분 × 5일
        job.cancel()
    }

    @Test
    fun withoutStudentInfoBalanceStillReportsWithoutVerdict() = runTest {
        val vm = OverviewViewModel(streams, today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertNull(s.stage); assertNull(s.currentPeriodLabel)
        assertEquals(BalanceVerdict.NONE, s.balance!!.studyVerdict); assertEquals(0, s.balance!!.experiencesThisPeriod)
        job.cancel()
    }

    @Test
    fun oneDigestPerConcernFromEachConcernsData() = runTest {
        streams.subjects.value = listOf(Fixtures.math)
        streams.topics.value = Fixtures.topics("math", 4, covered = 2, reviewed = 1)
        streams.grades.value = listOf(Fixtures.grade("math", 90.0, today.toEpochDay()))
        streams.growthRecords.value = listOf(Fixtures.growth(LocalDate.of(2029, 9, 1), height = 130.0))
        val vm = OverviewViewModel(streams, today = { today }); val job = subscribe(vm.state)
        val d = settle(vm.state).digests
        assertEquals(listOf(Concern.STUDY, Concern.LEARN, Concern.PROJECT, Concern.EXAMS, Concern.CLASS, Concern.GROWTH, Concern.DISCOVER), d.map { it.concern })
        assertEquals("복습 1/4단원", d[0].detail)
        assertEquals("복습할 단원 2개", d[1].headline); assertEquals("수학 · 단원 1", d[1].detail)
        assertEquals("진행 중인 프로젝트 없음", d[2].headline)
        assertEquals("다가오는 시험 없음", d[3].headline); assertEquals("최근 1번 평균 90점", d[3].detail)
        assertEquals("낸 과제 없음", d[4].headline)
        assertEquals("키 130cm", d[5].headline)
        assertEquals("이번 학기 활동 없음", d[6].headline)
        job.cancel()
    }

    @Test
    fun projectDigestFlagsAProjectThatFellBehind() = runTest {
        val plan = ProjectCatalog.byId.getValue("piano")
        val (goal, steps) = ProjectPlanner.start(plan, 1, today.minusYears(2), "PARENT")
        streams.goals.value = listOf(goal.copy(familyId = Fixtures.FAMILY))
        streams.goalSteps.value = steps
        val vm = OverviewViewModel(streams, today = { today }); val job = subscribe(vm.state)
        val d = settle(vm.state).digests.single { it.concern == Concern.PROJECT }
        assertEquals("프로젝트 1개 진행 중", d.headline)
        assertEquals("피아노 한 곡 완성 · 바이엘 · 계획보다 늦어요", d.detail)
        assertTrue(d.attention)
        job.cancel()
    }
}
