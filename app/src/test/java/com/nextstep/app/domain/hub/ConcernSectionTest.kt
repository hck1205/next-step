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
        assertEquals(listOf(ConcernSection.SELF, ConcernSection.PROGRESS, ConcernSection.TIME, ConcernSection.HABITS, ConcernSection.CALENDAR), ConcernSection.sectionsOf(Concern.STUDY, v))
        assertEquals(listOf(ConcernSection.CURRICULUM, ConcernSection.REVIEW, ConcernSection.CONTENT, ConcernSection.ROADMAP), ConcernSection.sectionsOf(Concern.LEARN, v))
        assertEquals(listOf(ConcernSection.GOAL_TREE, ConcernSection.TODO, ConcernSection.ASSIGNMENTS, ConcernSection.PLAN_HISTORY, ConcernSection.REWARDS), ConcernSection.sectionsOf(Concern.PLAN, v))
        assertEquals(listOf(ConcernSection.PROJECTS, ConcernSection.PROJECT_CATALOG), ConcernSection.sectionsOf(Concern.PROJECT, v))
        assertEquals(listOf(ConcernSection.FAMILY_CALENDAR, ConcernSection.FAMILY_TALK, ConcernSection.GROWTH_ALBUM), ConcernSection.sectionsOf(Concern.FAMILY, v))
        assertEquals(listOf(ConcernSection.OVERVIEW, ConcernSection.PERIOD_REPORT), ConcernSection.sectionsOf(Concern.OVERVIEW, v))
        assertEquals(Concern.FAMILY, ConcernSection.concernsFor(v)[2])
    }

    @Test
    fun mentorsSeeClassworkOnlyNotTheFamilysOwnRecords() {
        val v = HubViewer.MENTOR
        // 멘토 = 담당 과목을 가르치는 사람: 활동·재능(발견)과 신체(성장)·우리 가족 관심사가 없고, 공부에서 자기주도 주간 계획(스스로)도 빠짐
        assertEquals(listOf(Concern.OVERVIEW, Concern.PLAN, Concern.STUDY, Concern.LEARN, Concern.PROJECT, Concern.EXAMS), ConcernSection.concernsFor(v))
        listOf(ConcernSection.BODY, ConcernSection.SELF, ConcernSection.ACTIVITIES, ConcernSection.TALENT, ConcernSection.REWARDS, ConcernSection.FAMILY_CALENDAR, ConcernSection.FAMILY_TALK, ConcernSection.GROWTH_ALBUM, ConcernSection.PERIOD_REPORT).forEach { assertFalse(it in ConcernSection.visibleFor(v)) }
        assertEquals(ConcernSection.PROGRESS, ConcernSection.from("self", v)) // 가족 섹션을 열려고 하면 같은 관심사의 첫 섹션
        assertEquals(ConcernSection.OVERVIEW, ConcernSection.from("body", v))
        assertEquals(ConcernSection.OVERVIEW, ConcernSection.from("family-calendar", v)) // 가족 달력은 멘토에게 없음
        assertEquals(ConcernSection.GOAL_TREE, ConcernSection.visibleFor(v)[1])
    }

    @Test
    fun studentsPutLearningFirstAndTheHubGrowsWithLevel() {
        assertEquals(listOf(Concern.OVERVIEW, Concern.PLAN, Concern.FAMILY, Concern.LEARN, Concern.PROJECT, Concern.STUDY, Concern.EXAMS, Concern.DISCOVER, Concern.GROWTH), ConcernSection.concernsFor(HubViewer.student(StudentUiLevel.BRANCH)))
        assertEquals(ConcernSection.entries.toSet() - ConcernSection.PERIOD_REPORT, ConcernSection.visibleFor(HubViewer.student(StudentUiLevel.BRANCH)).toSet()) // 월간·학기 리포트는 학부모만
        assertEquals(listOf(ConcernSection.OVERVIEW, ConcernSection.REWARDS, ConcernSection.FAMILY_CALENDAR, ConcernSection.FAMILY_TALK, ConcernSection.GROWTH_ALBUM, ConcernSection.PROJECTS, ConcernSection.TIME, ConcernSection.ACTIVITIES, ConcernSection.BODY), ConcernSection.visibleFor(HubViewer.student(StudentUiLevel.SPROUT)))
        // 학령 전(씨앗): 공부·배울 것 관심사 자체가 없고, 교육 프로젝트는 진행 중인 것만(새로 시작은 줄기부터).
        // 오늘의 "나의 레벨" 카드가 여는 보상·배지는 씨앗부터 보임
        assertEquals(listOf(ConcernSection.OVERVIEW, ConcernSection.REWARDS, ConcernSection.FAMILY_CALENDAR, ConcernSection.FAMILY_TALK, ConcernSection.GROWTH_ALBUM, ConcernSection.PROJECTS, ConcernSection.ACTIVITIES, ConcernSection.BODY), ConcernSection.visibleFor(HubViewer.student(StudentUiLevel.SEED)))
        assertEquals(ConcernSection.REWARDS, ConcernSection.from("rewards", HubViewer.student(StudentUiLevel.SEED)))
        assertTrue(ConcernSection.PROJECT_CATALOG in ConcernSection.visibleFor(HubViewer.student(StudentUiLevel.STEM)))
        // 자기주도 사다리는 스스로 계획을 같이 세우기 시작하는 떡잎(초3)부터 학생에게 보임(어린 단계는 오늘 카드로만)
        assertFalse(ConcernSection.SELF in ConcernSection.visibleFor(HubViewer.student(StudentUiLevel.SPROUT)))
        assertEquals(ConcernSection.SELF, ConcernSection.sectionsOf(Concern.STUDY, HubViewer.student(StudentUiLevel.SEEDLING)).first())
        assertTrue(ConcernSection.REVIEW in ConcernSection.visibleFor(HubViewer.student(StudentUiLevel.SEEDLING)))
        assertFalse(ConcernSection.ASSIGNMENTS in ConcernSection.visibleFor(HubViewer.student(StudentUiLevel.SEEDLING)))
        assertTrue(ConcernSection.GOAL_TREE in ConcernSection.visibleFor(HubViewer.student(StudentUiLevel.SEEDLING)))
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
