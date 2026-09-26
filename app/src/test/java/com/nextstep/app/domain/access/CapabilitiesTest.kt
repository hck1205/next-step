package com.nextstep.app.domain.access

import com.nextstep.app.domain.gamify.GameStyle
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.hub.HubAudience
import com.nextstep.app.domain.selfdirection.LoopStep
import com.nextstep.app.domain.selfdirection.SelfDirectionStage
import com.nextstep.app.domain.year.YearDoer
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilitiesTest {
    @Test
    fun selfDirectionStepsFollowWhoOwnsThem() {
        val follow = SelfDirectionStage.FOLLOW; val together = SelfDirectionStage.PLAN_TOGETHER; val first = SelfDirectionStage.PLAN_FIRST
        assertTrue(parent.canDo(LoopStep.PLAN, follow)); assertFalse(student.canDo(LoopStep.PLAN, follow))
        assertTrue(student.canDo(LoopStep.REFLECT, follow)); assertTrue(parent.canDo(LoopStep.REFLECT, follow))
        assertTrue(student.canDo(LoopStep.PLAN, together)); assertTrue(parent.canDo(LoopStep.PLAN, together))
        assertTrue(student.canDo(LoopStep.PLAN, first)); assertFalse(parent.canDo(LoopStep.PLAN, first))
        SelfDirectionStage.entries.forEach { st -> LoopStep.entries.forEach { assertFalse(mentor.canDo(it, st)) } }
        assertTrue(parent.canApproveWeekPlan(first)); assertFalse(student.canApproveWeekPlan(first)); assertFalse(parent.canApproveWeekPlan(together))
        assertTrue(parent.seesWeekDetails(SelfDirectionStage.SELF)); assertFalse(parent.seesWeekDetails(SelfDirectionStage.OWN)); assertTrue(student.seesWeekDetails(SelfDirectionStage.OWN))
        assertTrue(parent.canChooseSelfDirection); assertFalse(student.canChooseSelfDirection); assertFalse(mentor.canChooseSelfDirection)
    }

    @Test
    fun parentsAndMentorsGiveRewardsAndOnlyParentsToggleGames() {
        assertTrue(parent.canGiveRewards); assertTrue(mentor.canGiveRewards); assertTrue(parentMentor.canGiveRewards); assertFalse(student.canGiveRewards)
        GameStyle.entries.forEach { assertTrue(parent.canToggleGamification(it)); assertFalse(mentor.canToggleGamification(it)) }
        // 스스로 끌 수 있는 것은 성장 기록 모양(중등 이후)의 학생뿐
        assertFalse(student.canToggleGamification(GameStyle.STICKERS)); assertFalse(student.canToggleGamification(GameStyle.LEVELS))
        assertTrue(student.canToggleGamification(GameStyle.GROWTH))
    }

    @Test
    fun factoryTakesMentorFlagFromMemberUnlessRoleIsMentor() {
        assertFalse(Capabilities.of(Role.PARENT, null).actsAsMentor)
        assertFalse(Capabilities.of(Role.PARENT, Fixtures.member(Role.PARENT, "엄마")).actsAsMentor)
        assertTrue(Capabilities.of(Role.PARENT, Fixtures.member(Role.PARENT, "엄마", mentorEnabled = true)).actsAsMentor)
        assertTrue(Capabilities.of(Role.MENTOR, null).actsAsMentor)
    }

    @Test
    fun onlyParentsAddChildrenAndNonStudentsLinkThem() {
        assertTrue(parent.canAddChildren); assertFalse(mentor.canAddChildren); assertFalse(student.canAddChildren)
        assertTrue(parent.canLinkChildren); assertTrue(mentor.canLinkChildren); assertFalse(student.canLinkChildren)
    }

    @Test
    fun familyManagementIsForStudentAndParent() {
        assertTrue(student.canRemoveMembers); assertTrue(parent.canRemoveMembers); assertFalse(mentor.canRemoveMembers)
        assertTrue(parent.canToggleMentorMode); assertTrue(parentMentor.canToggleMentorMode)
        assertFalse(student.canToggleMentorMode); assertFalse(mentor.canToggleMentorMode)
        assertTrue(parent.canChooseStudentScreen); assertFalse(student.canChooseStudentScreen); assertFalse(mentor.canChooseStudentScreen)
    }

    private val student = Capabilities(Role.STUDENT, mentorEnabled = false)
    private val parent = Capabilities(Role.PARENT, mentorEnabled = false)
    private val parentMentor = Capabilities(Role.PARENT, mentorEnabled = true)
    private val mentor = Capabilities(Role.MENTOR, mentorEnabled = true)

    @Test
    fun studentOwnsPersonalRecordsButNotCuration() {
        assertTrue(student.canMarkTopicStatus); assertTrue(student.canUseTimer); assertTrue(student.canGeneratePlan)
        assertTrue(student.canCompleteTasks); assertTrue(student.canUpdateRoadmapProgress); assertTrue(student.canEditTopics)
        assertFalse(student.canEditRoadmap); assertFalse(student.actsAsMentor)
        assertEquals("STUDENT", student.actingRoleName)
    }

    @Test
    fun plainParentObservesAndEncouragesOnly() {
        assertFalse(parent.canEditSubjects); assertFalse(parent.canEditTopics); assertFalse(parent.canCreateTasks)
        assertFalse(parent.canEditRoadmap); assertFalse(parent.canApplyInsightActions); assertFalse(parent.canUseTimer)
        assertTrue(parent.canEditEvents); assertTrue(parent.canEditGrades)
        assertEquals("PARENT", parent.actingRoleName)
    }

    @Test
    fun parentAsMentorGainsMentorPowersAndActsAsMentor() {
        assertTrue(parentMentor.actsAsMentor)
        assertTrue(parentMentor.canEditRoadmap); assertTrue(parentMentor.canCreateTasks); assertTrue(parentMentor.canEditTopics)
        assertFalse(parentMentor.canMarkTopicStatus); assertFalse(parentMentor.canCompleteTasks)
        assertEquals("MENTOR", parentMentor.actingRoleName)
    }

    @Test
    fun mentorCuratesButNeverRecordsForTheStudent() {
        assertTrue(mentor.canEditRoadmap); assertTrue(mentor.canEditTopics); assertTrue(mentor.canApplyInsightActions)
        assertFalse(mentor.canMarkTopicStatus); assertFalse(mentor.canUpdateRoadmapProgress); assertFalse(mentor.canUseTimer)
        assertEquals("MENTOR", mentor.actingRoleName)
    }

    @Test
    fun hubAudienceFollowsRoleAndParentMentorStaysParent() {
        assertEquals(HubAudience.STUDENT, student.hubAudience)
        assertEquals(HubAudience.PARENT, parent.hubAudience)
        assertEquals(HubAudience.PARENT, parentMentor.hubAudience)
        assertEquals(HubAudience.MENTOR, mentor.hubAudience)
    }

    @Test
    fun yearDoersFollowRole() {
        assertEquals(setOf(YearDoer.CHILD, YearDoer.TOGETHER), student.yearDoers)
        assertEquals(setOf(YearDoer.PARENT, YearDoer.TOGETHER), parent.yearDoers)
        assertEquals(setOf(YearDoer.PARENT, YearDoer.TOGETHER, YearDoer.MENTOR), parentMentor.yearDoers)
        assertEquals(setOf(YearDoer.MENTOR), mentor.yearDoers)
    }
}
