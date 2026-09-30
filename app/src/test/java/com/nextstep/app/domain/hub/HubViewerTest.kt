package com.nextstep.app.domain.hub

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.growth.StudentUiLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class HubViewerTest {
    @Test
    fun onlyStudentsCarryALevel() {
        assertEquals(HubViewer.student(StudentUiLevel.SPROUT), HubViewer.of(Capabilities(Role.STUDENT, false), StudentUiLevel.SPROUT))
        assertEquals(HubViewer.student(StudentUiLevel.TREE), HubViewer.of(Capabilities(Role.STUDENT, false), null))
        assertEquals(HubViewer.PARENT, HubViewer.of(Capabilities(Role.PARENT, true), StudentUiLevel.SPROUT))
        assertEquals(HubViewer.MENTOR, HubViewer.of(Capabilities(Role.MENTOR, true), StudentUiLevel.SPROUT))
    }
}
