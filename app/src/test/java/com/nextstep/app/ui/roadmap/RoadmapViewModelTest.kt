package com.nextstep.app.ui.roadmap

import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeRoadmapRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class RoadmapViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams()
    private val roadmap = FakeRoadmapRepository()

    private fun vm() = RoadmapViewModel(streams, roadmap)

    @Test
    fun stateSplitsActiveDoneAndBuildsSuggestions() = runTest {
        streams.subjects.value = listOf(Fixtures.math)
        streams.topics.value = Fixtures.topics("math", 4, covered = 2, reviewed = 1)
        streams.roadmap.value = listOf(Fixtures.roadmap("a"), Fixtures.roadmap("b", status = RoadmapStatus.DONE), Fixtures.roadmap("c", contentId = "c-영상"))
        streams.contents.value = listOf(Fixtures.content("영상"))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(listOf("a", "c"), s.active.map { it.title }); assertEquals(1, s.done.size)
        assertEquals(1f / 3f, s.completion, 0.001f)
        assertEquals(listOf("복습 보강: 단원 1", "선행 예습: 단원 2"), s.suggestions.map { it.second })
        assertEquals("영상", s.contentOf(s.active[1])!!.title); assertNull(s.contentOf(s.active[0]))
        job.cancel()
    }

    @Test
    fun saveKeepsExistingIdAndSetsOrderForNew() = runTest {
        streams.roadmap.value = listOf(Fixtures.roadmap("x"), Fixtures.roadmap("y"))
        val vm = vm(); val job = subscribe(vm.state); settle(vm.state)
        vm.onEvent(RoadmapEvent.Save(null, "math", "새 항목", "설명", "교재", LocalDate.of(2026, 10, 1), "c1"))
        val existing = Fixtures.roadmap("x")
        vm.onEvent(RoadmapEvent.Save(existing, null, "x2", "", "", null, null))
        vm.onEvent(RoadmapEvent.SetStatus("r-x", RoadmapStatus.IN_PROGRESS)); vm.onEvent(RoadmapEvent.Delete("r-y"))
        vm.onEvent(RoadmapEvent.AddSuggestion(Fixtures.math, "복습 보강: 단원 1"))
        settle(vm.state)
        val created = roadmap.saved[0]
        assertEquals(2, created.orderIndex); assertEquals("c1", created.contentId); assertEquals(LocalDate.of(2026, 10, 1).toEpochDay(), created.targetDate)
        assertEquals(existing.id, roadmap.saved[1].id); assertEquals("x2", roadmap.saved[1].title); assertNull(roadmap.saved[1].targetDate)
        assertEquals(listOf("r-x" to RoadmapStatus.IN_PROGRESS), roadmap.statuses); assertEquals(listOf("r-y"), roadmap.deleted)
        assertEquals("math", roadmap.saved[2].subjectId); assertEquals(LocalDate.now().plusDays(7).toEpochDay(), roadmap.saved[2].targetDate)
        job.cancel()
    }
}
