package com.nextstep.app.domain.access

import com.nextstep.app.data.model.Role
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilitiesTest {
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
}
