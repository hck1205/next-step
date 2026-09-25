package com.nextstep.app.domain.hub

import com.nextstep.app.domain.growth.StudentUiLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConcernSectionTest {
    @Test
    fun parentsSeeEveryConcernInTheirOrder() {
        val v = HubViewer.PARENT
        assertEquals(HubAudience.PARENT.order, ConcernSection.concernsFor(v))
        assertEquals(ConcernSection.entries.toSet(), ConcernSection.visibleFor(v).toSet())
        assertEquals(ConcernSection.OVERVIEW, ConcernSection.visibleFor(v).first())
        assertEquals(ConcernSection.entries.size, ConcernSection.entries.map { it.route }.toSet().size)
        assertEquals(listOf(ConcernSection.PROGRESS, ConcernSection.TIME, ConcernSection.HABITS, ConcernSection.CALENDAR), ConcernSection.sectionsOf(Concern.STUDY, v))
        assertEquals(listOf(ConcernSection.CURRICULUM, ConcernSection.REVIEW, ConcernSection.CONTENT, ConcernSection.ROADMAP), ConcernSection.sectionsOf(Concern.LEARN, v))
        assertEquals(listOf(ConcernSection.ASSIGNMENTS), ConcernSection.sectionsOf(Concern.CLASS, v))
    }

    @Test
    fun mentorsPutClassworkFirstAndDoNotSeeBodyRecords() {
        val v = HubViewer.MENTOR
        assertEquals(listOf(Concern.OVERVIEW, Concern.CLASS, Concern.STUDY, Concern.LEARN, Concern.EXAMS, Concern.DISCOVER), ConcernSection.concernsFor(v))
        assertFalse(ConcernSection.BODY in ConcernSection.visibleFor(v))
        assertEquals(ConcernSection.OVERVIEW, ConcernSection.from("body", v))
        assertEquals(ConcernSection.ASSIGNMENTS, ConcernSection.visibleFor(v)[1])
    }

    @Test
    fun studentsPutLearningFirstAndTheHubGrowsWithLevel() {
        assertEquals(listOf(Concern.OVERVIEW, Concern.LEARN, Concern.STUDY, Concern.EXAMS, Concern.CLASS, Concern.DISCOVER, Concern.GROWTH), ConcernSection.concernsFor(HubViewer.student(StudentUiLevel.BRANCH)))
        assertEquals(ConcernSection.entries.toSet(), ConcernSection.visibleFor(HubViewer.student(StudentUiLevel.BRANCH)).toSet())
        assertEquals(listOf(ConcernSection.OVERVIEW, ConcernSection.TIME, ConcernSection.ACTIVITIES, ConcernSection.BODY), ConcernSection.visibleFor(HubViewer.student(StudentUiLevel.SPROUT)))
        // 학령 전(씨앗): 공부·배울 것 관심사 자체가 없음
        assertEquals(listOf(ConcernSection.OVERVIEW, ConcernSection.ACTIVITIES, ConcernSection.BODY), ConcernSection.visibleFor(HubViewer.student(StudentUiLevel.SEED)))
        assertTrue(ConcernSection.REVIEW in ConcernSection.visibleFor(HubViewer.student(StudentUiLevel.SEEDLING)))
        assertFalse(ConcernSection.ASSIGNMENTS in ConcernSection.visibleFor(HubViewer.student(StudentUiLevel.SEEDLING)))
        StudentUiLevel.entries.zipWithNext().forEach { (younger, older) ->
            assertTrue(ConcernSection.visibleFor(HubViewer.student(older)).containsAll(ConcernSection.visibleFor(HubViewer.student(younger))))
        }
    }

    @Test
    fun routesFallBackToAVisibleSection() {
        val sprout = HubViewer.student(StudentUiLevel.SPROUT)
        assertEquals(ConcernSection.GRADES, ConcernSection.from("grades", HubViewer.PARENT))
        assertEquals(ConcernSection.OVERVIEW, ConcernSection.from(null, HubViewer.PARENT))
        assertEquals(ConcernSection.OVERVIEW, ConcernSection.from("unknown", HubViewer.PARENT))
        assertEquals(ConcernSection.TIME, ConcernSection.from("progress", sprout)) // 같은 관심사의 첫 섹션
        assertEquals(ConcernSection.OVERVIEW, ConcernSection.from("grades", sprout)) // 관심사 자체가 안 보임
        assertEquals(ConcernSection.OVERVIEW, ConcernSection.from("time", HubViewer.student(StudentUiLevel.SEED)))
        assertEquals(ConcernSection.PROGRESS, ConcernSection.from("progress", HubViewer.student(StudentUiLevel.SEEDLING)))
    }
}
