package com.nextstep.app.data.school

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.school.School
import com.nextstep.app.domain.school.SchoolDay
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeFamilyEventRepository
import com.nextstep.app.fake.FakeNeisApi
import com.nextstep.app.testing.Fixtures
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NeisSchoolServiceTest {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val events = FakeFamilyEventRepository(streams)
    private val today = LocalDate.of(2029, 4, 1)

    @Test
    fun syncBringsTheStudentsSchoolYearOnce() = runTest {
        val api = FakeNeisApi(days = listOf(SchoolDay(LocalDate.of(2029, 4, 25), "1학기 중간고사"), SchoolDay(LocalDate.of(2029, 5, 4), "재량휴업일", dayOff = true)))
        val service = NeisSchoolService(api, streams.members, events)
        assertEquals(0, service.syncNow(today).getOrThrow()) // 학생이 없으면 아무것도 안 함
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "지우", id = "kid", gradeYear = 5).copy(schoolCode = "B10:7010057", schoolName = "대치초"))
        assertEquals(2, service.syncNow(today).getOrThrow())
        assertEquals(listOf("schedule:B10:7010057:2029-03-01:2030-02-28"), api.asked)
        assertEquals(setOf("kid"), streams.familyEvents.value.map { it.memberIds }.toSet())
        assertEquals(0, service.syncNow(today).getOrThrow()) // 다시 받아도 새것만
        assertTrue(NeisSchoolService(FakeNeisApi(available = false), streams.members, events).search("대치").getOrThrow().isEmpty())
        api.schools = listOf(School("B10", "7010057", "대치초"))
        assertEquals(listOf("대치초"), service.search("대치").getOrThrow().map { it.name })
    }
}
