package com.nextstep.app.domain.hub

import com.nextstep.app.domain.growth.StudentUiLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConcernSectionTest {
    @Test
    fun adultsSeeEveryConcernAndOverviewComesFirst() {
        assertEquals(Concern.entries, ConcernSection.concernsFor(null))
        assertEquals(ConcernSection.entries, ConcernSection.visibleFor(null))
        assertEquals(ConcernSection.OVERVIEW, ConcernSection.visibleFor(null).first())
        assertEquals(ConcernSection.entries.size, ConcernSection.entries.map { it.route }.toSet().size)
        assertEquals(
            listOf(ConcernSection.PROGRESS, ConcernSection.TIME, ConcernSection.CALENDAR, ConcernSection.CURRICULUM, ConcernSection.CONTENT, ConcernSection.ROADMAP),
            ConcernSection.sectionsOf(Concern.STUDY, null),
        )
    }

    @Test
    fun youngStudentsSeeFewerSectionsAndTheHubGrowsWithLevel() {
        assertEquals(listOf(ConcernSection.OVERVIEW, ConcernSection.TIME, ConcernSection.BODY, ConcernSection.ACTIVITIES), ConcernSection.visibleFor(StudentUiLevel.SPROUT))
        assertEquals(listOf(Concern.OVERVIEW, Concern.STUDY, Concern.GROWTH, Concern.DISCOVER), ConcernSection.concernsFor(StudentUiLevel.SPROUT))
        // 학령 전(씨앗): 공부 관심사 자체가 없음
        assertEquals(listOf(ConcernSection.OVERVIEW, ConcernSection.BODY, ConcernSection.ACTIVITIES), ConcernSection.visibleFor(StudentUiLevel.SEED))
        assertEquals(ConcernSection.OVERVIEW, ConcernSection.from("time", StudentUiLevel.SEED))
        StudentUiLevel.entries.zipWithNext().forEach { (younger, older) ->
            assertTrue(ConcernSection.visibleFor(older).containsAll(ConcernSection.visibleFor(younger)))
        }
        assertEquals(ConcernSection.entries, ConcernSection.visibleFor(StudentUiLevel.BRANCH))
    }

    @Test
    fun routesFallBackToAVisibleSection() {
        assertEquals(ConcernSection.GRADES, ConcernSection.from("grades", null))
        assertEquals(ConcernSection.OVERVIEW, ConcernSection.from(null, null))
        assertEquals(ConcernSection.OVERVIEW, ConcernSection.from("unknown", null))
        assertEquals(ConcernSection.TIME, ConcernSection.from("progress", StudentUiLevel.SPROUT)) // 같은 관심사의 첫 섹션
        assertEquals(ConcernSection.OVERVIEW, ConcernSection.from("grades", StudentUiLevel.SPROUT)) // 관심사 자체가 안 보임
        assertEquals(ConcernSection.PROGRESS, ConcernSection.from("progress", StudentUiLevel.SEEDLING))
    }
}
