package com.nextstep.app.ui.grades

import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.model.ExamType
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeGradeRepository
import com.nextstep.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class GradesViewModelTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private val streams = FakeFamilyDataStreams()
    private val grades = FakeGradeRepository()
    private val math = SubjectEntity(id = "math", familyId = "fam", name = "수학", color = 0xFF3B82F6)

    private fun viewModel() = GradesViewModel(streams, grades)

    /** WhileSubscribed 라 구독자가 있어야 state 가 흐릅니다. */
    private fun kotlinx.coroutines.test.TestScope.subscribe(vm: GradesViewModel): Job = launch { vm.state.collect {} }

    @Test
    fun stateComputesScoresAndOverallAverage() = runTest {
        streams.subjects.value = listOf(math)
        streams.grades.value = listOf(
            GradeEntity(familyId = "fam", subjectId = "math", title = "1차", score = 80.0, date = 1),
            GradeEntity(familyId = "fam", subjectId = "math", title = "2차", score = 90.0, date = 2),
        )
        val vm = viewModel()
        val job = subscribe(vm)
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(85.0, state.overallAverage!!, 0.001)
        assertEquals(1, state.scores.size)
        assertEquals(10.0, state.scores.single().trend!!, 0.001)
        job.cancel()
    }

    @Test
    fun saveEventDelegatesToRepositoryWithConvertedDate() = runTest {
        val vm = viewModel()
        val job = subscribe(vm)
        vm.onEvent(GradesEvent.Save(null, "math", "중간고사", ExamType.MIDTERM, 88.0, 100.0, 70.0, LocalDate.of(2026, 9, 1), ""))
        advanceUntilIdle()

        val saved = grades.saved.single()
        assertEquals("math", saved.subjectId)
        assertEquals(LocalDate.of(2026, 9, 1).toEpochDay(), saved.date)
        assertEquals(70.0, saved.classAverage!!, 0.001)
        job.cancel()
    }

    @Test
    fun filterNarrowsListWithoutTouchingRepository() = runTest {
        streams.subjects.value = listOf(math, SubjectEntity(id = "eng", familyId = "fam", name = "영어", color = 0xFF10B981))
        streams.grades.value = listOf(
            GradeEntity(familyId = "fam", subjectId = "math", title = "a", score = 80.0, date = 1),
            GradeEntity(familyId = "fam", subjectId = "eng", title = "b", score = 90.0, date = 1),
        )
        val vm = viewModel()
        val job = subscribe(vm)
        vm.onEvent(GradesEvent.SetFilter("eng"))
        advanceUntilIdle()

        assertEquals(listOf("b"), vm.state.value.filtered.map { it.title })
        assertNull(grades.saved.firstOrNull())
        job.cancel()
    }
}
