package com.nextstep.app.ui.lessons

import com.nextstep.app.data.local.entity.LessonEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.lesson.LessonPlan
import com.nextstep.app.domain.lesson.LessonStatus
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeLessonRepository
import com.nextstep.app.fake.FakeMemberRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonsViewModelTest : ViewModelTestBase() {
    private val today = LocalDate.of(2029, 3, 6) // 화요일
    private val kim = Fixtures.member(Role.MENTOR, "김쌤", id = "me").copy(lessonDays = "2,4", lessonStart = 960, lessonEnd = 1050, tuitionFee = 300_000, tuitionDay = 8)
    private val lee = Fixtures.member(Role.MENTOR, "이쌤", id = "lee").copy(lessonDays = "6", lessonStart = 600, lessonEnd = 660)

    private fun streams(role: Role) = FakeFamilyDataStreams(role = role).apply {
        members.value = listOf(kim, lee, Fixtures.member(Role.PARENT, "엄마"))
        myMember.value = if (role == Role.MENTOR) kim else Fixtures.member(Role.PARENT, "엄마")
        lessons.value = listOf(LessonEntity(familyId = Fixtures.FAMILY, mentorId = "lee", date = LocalDate.of(2029, 3, 3).toEpochDay(), status = "ABSENT"))
    }

    @Test
    fun parentSeesEveryMentorsMonthWithTuition() = runTest {
        val streams = streams(Role.PARENT)
        val vm = LessonsViewModel(streams, FakeMemberRepository(), FakeLessonRepository(streams), today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(listOf("김쌤", "이쌤"), s.books.map { it.mentorName })
        assertEquals(9, s.books[0].days.size); assertEquals(2, s.books[0].tuition!!.daysLeft)
        assertEquals("수업 0/5회 · 결석 1", s.books[1].summary.line)
        vm.onEvent(LessonsEvent.MoveMonth(1))
        assertEquals(YearMonth.of(2029, 4), settle(vm.state).month)
        job.cancel()
    }

    @Test
    fun mentorSeesOnlyOwnBookAndWrites() = runTest {
        val streams = streams(Role.MENTOR); val members = FakeMemberRepository()
        val vm = LessonsViewModel(streams, members, FakeLessonRepository(streams), today = { today }); val job = subscribe(vm.state)
        assertEquals(listOf("김쌤"), settle(vm.state).books.map { it.mentorName }) // 다른 멘토의 일정·수업료는 보지 않음
        vm.onEvent(LessonsEvent.Mark(today, LessonStatus.MAKEUP))
        val s = settle(vm.state)
        assertEquals(LessonStatus.MAKEUP, s.books.single().days.first { it.date == today }.status)
        assertTrue(s.myPlan.isSet)
        vm.onEvent(LessonsEvent.SavePlan(LessonPlan(setOf(DayOfWeek.FRIDAY), 900, 960)))
        settle(vm.state)
        assertEquals(listOf("lessonPlan:me:5:900-960:0@0"), members.calls)
        job.cancel()
    }
}
