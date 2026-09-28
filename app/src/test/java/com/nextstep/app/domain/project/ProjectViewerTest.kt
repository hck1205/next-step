package com.nextstep.app.domain.project

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.testing.Fixtures
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectViewerTest {
    private val today = LocalDate.of(2029, 3, 7)

    private fun project(byRole: String, byId: String = ""): GoalEntity =
        ProjectPlanner.start(ProjectCatalog.byId.getValue("piano"), 0, today, byRole).first.copy(createdById = byId)

    private val student = ProjectViewer(Role.STUDENT, "kid")
    private val mom = ProjectViewer(Role.PARENT, "mom")
    private val dad = ProjectViewer(Role.PARENT, "dad")
    private val mathMentor = ProjectViewer(Role.MENTOR, "m1")
    private val pianoMentor = ProjectViewer(Role.MENTOR, "m2")

    @Test
    fun kindComesFromWhoStartedIt() {
        assertEquals(ProjectKind.SELF, ProjectKind.of(project("STUDENT")))
        assertEquals(ProjectKind.PARENT, ProjectKind.of(project("PARENT")))
        assertEquals(ProjectKind.MENTOR, ProjectKind.of(project("MENTOR")))
        assertNull(ProjectKind.of(project("")))
        assertEquals(ProjectKind.MENTOR, ProjectKind.startedBy(Role.MENTOR))
        assertEquals(ProjectKind.SELF, ProjectKind.startedBy(null))
    }

    @Test
    fun onlyTheCreatorAndTheStudentSeeAProject() {
        val self = project("STUDENT", "kid")
        assertTrue(student.canSee(self)); assertFalse(mom.canSee(self)); assertFalse(mathMentor.canSee(self))

        val fromMom = project("PARENT", "mom")
        assertTrue(student.canSee(fromMom)); assertTrue(mom.canSee(fromMom))
        assertFalse(dad.canSee(fromMom)); assertFalse(mathMentor.canSee(fromMom))

        val fromMentor = project("MENTOR", "m2")
        assertTrue(student.canSee(fromMentor)); assertTrue(pianoMentor.canSee(fromMentor))
        assertFalse(mathMentor.canSee(fromMentor)); assertFalse(mom.canSee(fromMentor))
    }

    @Test
    fun oldRecordsFallBackToTheRoleAndOtherGoalsAreOpen() {
        val oldFromParent = project("PARENT")
        assertTrue(mom.canSee(oldFromParent)); assertTrue(dad.canSee(oldFromParent)); assertFalse(mathMentor.canSee(oldFromParent))
        assertTrue(mathMentor.canSee(project("")))
        assertTrue(mathMentor.canSee(Fixtures.goal("수학 목표").copy(createdByRole = "STUDENT")))
        assertTrue(ProjectViewer(null, null).canSee(project("STUDENT")))
    }
}
