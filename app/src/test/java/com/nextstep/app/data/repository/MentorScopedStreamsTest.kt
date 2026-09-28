package com.nextstep.app.data.repository

import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.testing.Fixtures
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MentorScopedStreamsTest {
    private val today = LocalDate.of(2029, 10, 10)

    private fun family(role: Role) = FakeFamilyDataStreams(role = role).apply {
        subjects.value = listOf(Fixtures.math, Fixtures.english)
        grades.value = listOf(Fixtures.grade("math", 80.0, 1), Fixtures.grade("eng", 90.0, 1))
        tasks.value = listOf(Fixtures.task("수학 과제", today, "math", by = "MENTOR"), Fixtures.task("영어", today, "eng"), Fixtures.task("과목 없음", today))
        myMember.value = Fixtures.member(role, "나", id = "me", subjectIds = "math")
    }

    @Test
    fun mentorSeesOnlyAssignedSubjectsAndGeneralTasks() = runTest {
        val s = MentorScopedStreams(family(Role.MENTOR))
        assertEquals(listOf("수학"), s.subjects.first().map { it.name })
        assertEquals(listOf("math"), s.grades.first().map { it.subjectId })
        assertEquals(listOf("수학 과제", "과목 없음"), s.tasks.first().map { it.title })
    }

    @Test
    fun familyAndMentorWithoutChosenSubjectsSeeEverything() = runTest {
        assertEquals(2, MentorScopedStreams(family(Role.PARENT)).grades.first().size) // 학부모 겸 멘토라도 학부모 자리
        assertEquals(3, MentorScopedStreams(family(Role.STUDENT)).tasks.first().size)
        val noChoice = family(Role.MENTOR).apply { myMember.value = Fixtures.member(Role.MENTOR, "나", id = "me") }
        assertEquals(2, MentorScopedStreams(noChoice).subjects.first().size)
    }
}
