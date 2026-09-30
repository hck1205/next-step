package com.nextstep.app.ui.content

import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.fake.FakeContentRepository
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams()
    private val contents = FakeContentRepository()

    private fun vm() = ContentViewModel(streams, contents)

    @Test
    fun filtersNarrowByQuerySubjectTypeLevelAndWatched() = runTest {
        streams.subjects.value = listOf(Fixtures.math)
        streams.contents.value = listOf(
            Fixtures.content("수학 개념", "수학", ContentType.CONCEPT).copy(gradeLevel = GradeLevel.MIDDLE),
            Fixtures.content("수학 문제", "수학", ContentType.PROBLEM).copy(gradeLevel = GradeLevel.HIGH, watched = true),
            Fixtures.content("영어", "영어", ContentType.CONCEPT),
        )
        val vm = vm(); val job = subscribe(vm.state)
        assertEquals(3, settle(vm.state).filtered.size)
        assertEquals(listOf("수학", "영어"), settle(vm.state).subjectKeys)
        vm.onEvent(ContentEvent.SetSubject("수학")); assertEquals(2, settle(vm.state).filtered.size)
        vm.onEvent(ContentEvent.SetType(ContentType.PROBLEM)); assertEquals(listOf("수학 문제"), settle(vm.state).filtered.map { it.title })
        vm.onEvent(ContentEvent.SetType(null)); vm.onEvent(ContentEvent.SetLevel(GradeLevel.MIDDLE))
        assertEquals(listOf("수학 개념"), settle(vm.state).filtered.map { it.title }) // ALL 은 포함, HIGH 는 제외
        vm.onEvent(ContentEvent.SetLevel(null)); vm.onEvent(ContentEvent.ToggleHideWatched)
        assertEquals(listOf("수학 개념"), settle(vm.state).filtered.map { it.title })
        vm.onEvent(ContentEvent.ToggleHideWatched); vm.onEvent(ContentEvent.SetSubject(null)); vm.onEvent(ContentEvent.SetQuery("영"))
        assertEquals(listOf("영어"), settle(vm.state).filtered.map { it.title })
        job.cancel()
    }

    @Test
    fun addFlowAnalyzesThenSavesWithDraftAndResets() = runTest {
        contents.prepareResult = Result.success(FakeContentRepository.draft(title = "가져온 제목"))
        val vm = vm(); val job = subscribe(vm.state)
        vm.onEvent(ContentEvent.Analyze); settle(vm.state) // 빈 URL 은 무시
        assertNull(vm.state.value.add.draft)
        vm.onEvent(ContentEvent.SetUrl("https://youtu.be/abcdefghijk")); vm.onEvent(ContentEvent.Analyze)
        val s = settle(vm.state)
        assertEquals("가져온 제목", s.add.draft!!.title); assertNull(s.add.error)
        vm.onEvent(ContentEvent.Save("제목", "채널", "수학", GradeLevel.MIDDLE, ContentType.CONCEPT, "a, b", "요약", 12))
        val after = settle(vm.state)
        val saved = contents.saved.single()
        assertEquals("abcdefghijk", saved.videoId); assertEquals("수학", saved.subjectKey); assertEquals(12, saved.durationMinutes)
        assertNull(after.add.draft); assertEquals("", after.add.url)
        job.cancel()
    }

    @Test
    fun analyzeFailureShowsErrorAndSaveWithoutDraftIsIgnored() = runTest {
        contents.prepareResult = Result.failure(IllegalArgumentException("유튜브 링크가 아니에요"))
        val vm = vm(); val job = subscribe(vm.state)
        vm.onEvent(ContentEvent.SetUrl("x")); vm.onEvent(ContentEvent.Analyze)
        assertEquals("유튜브 링크가 아니에요", settle(vm.state).add.error)
        vm.onEvent(ContentEvent.Save("t", "", "", GradeLevel.ALL, ContentType.OTHER, "", "", 0))
        settle(vm.state)
        assertTrue(contents.saved.isEmpty())
        vm.onEvent(ContentEvent.ResetAdd)
        assertNull(settle(vm.state).add.error)
        job.cancel()
    }

    @Test
    fun otherEventsDelegate() = runTest {
        val vm = vm(); val job = subscribe(vm.state)
        val c = Fixtures.content("x")
        vm.onEvent(ContentEvent.Update(c)); vm.onEvent(ContentEvent.Rate("x", 4)); vm.onEvent(ContentEvent.SetWatched("x", true)); vm.onEvent(ContentEvent.Delete("x"))
        settle(vm.state)
        assertEquals(listOf(c), contents.saved); assertEquals(listOf("x" to 4), contents.rated); assertEquals(listOf("x" to true), contents.watched); assertEquals(listOf("x"), contents.deleted)
        assertNotNull(vm.state.value.recommendations)
        job.cancel()
    }
}
