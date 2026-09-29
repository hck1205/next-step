package com.nextstep.app.data.repository

import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.testing.Fixtures
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MentorScopedStreamsTest {
    private val today = LocalDate.of(2029, 10, 10)

    private fun family(role: Role) = FakeFamilyDataStreams(role = role).apply {
        subjects.value = listOf(Fixtures.math, Fixtures.english)
        grades.value = listOf(Fixtures.grade("math", 80.0, 1), Fixtures.grade("eng", 90.0, 1))
        tasks.value = listOf(Fixtures.task("수학 과제", today, "math", by = "MENTOR"), Fixtures.task("영어", today, "eng"), Fixtures.task("과목 없음", today))
        myMember.value = Fixtures.member(role, "나", id = "me", subjectIds = "math")
        familyEvents.value = listOf(Fixtures.familyEvent("할머니 댁", today))
        cheers.value = listOf(com.nextstep.app.data.local.entity.CheerEntity(familyId = Fixtures.FAMILY, taskId = "t", taskTitle = "분수", kind = "CLAP"))
        reportLogs.value = listOf(
            com.nextstep.app.data.local.entity.ReportLogEntity(familyId = Fixtures.FAMILY, kind = "WEEK", title = "내 리포트", sentById = "me"),
            com.nextstep.app.data.local.entity.ReportLogEntity(familyId = Fixtures.FAMILY, kind = "WEEK", title = "다른 쌤", sentById = "other"),
        )
    }

    @Test
    fun mentorSeesOnlyAssignedSubjectsAndGeneralTasks() = runTest {
        val s = MentorScopedStreams(family(Role.MENTOR))
        assertEquals(listOf("수학"), s.subjects.first().map { it.name })
        assertEquals(listOf("math"), s.grades.first().map { it.subjectId })
        assertEquals(listOf("수학 과제", "과목 없음"), s.tasks.first().map { it.title })
        assertTrue(s.familyEvents.first().isEmpty()) // 가족 달력은 멘토에게 한 건도 넘기지 않음
        assertTrue(s.cheers.first().isEmpty()) // 응원도 가족 사이의 일
        assertEquals(listOf("내 리포트"), s.reportLogs.first().map { it.title }) // 보낸 기록은 내 것만
    }

    @Test
    fun familyAndMentorWithoutChosenSubjectsSeeEverything() = runTest {
        assertEquals(2, MentorScopedStreams(family(Role.PARENT)).grades.first().size) // 학부모 겸 멘토라도 학부모 자리
        assertEquals(3, MentorScopedStreams(family(Role.STUDENT)).tasks.first().size)
        assertEquals(listOf("할머니 댁"), MentorScopedStreams(family(Role.PARENT)).familyEvents.first().map { it.title })
        assertEquals(1, MentorScopedStreams(family(Role.STUDENT)).familyEvents.first().size)
        assertEquals(1, MentorScopedStreams(family(Role.STUDENT)).cheers.first().size)
        assertEquals(2, MentorScopedStreams(family(Role.PARENT)).reportLogs.first().size)
        val noChoice = family(Role.MENTOR).apply { myMember.value = Fixtures.member(Role.MENTOR, "나", id = "me") }
        assertEquals(2, MentorScopedStreams(noChoice).subjects.first().size)
    }
}
