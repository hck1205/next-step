package com.nextstep.app.domain.roadmap

import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RoadmapTemplatesTest {
    @Test
    fun keepsOrderAndSpacingAndMovesToANewStart() {
        val items = listOf(
            Fixtures.roadmap("약분", "math", RoadmapStatus.DONE, LocalDate.of(2029, 3, 10)).copy(orderIndex = 0, description = "교과서 3단원", resource = "EBS"),
            Fixtures.roadmap("통분", "math", RoadmapStatus.IN_PROGRESS, LocalDate.of(2029, 3, 17)).copy(orderIndex = 1),
            Fixtures.roadmap("분수의 덧셈", "math").copy(orderIndex = 2, targetDate = null),
            Fixtures.roadmap("지움", "math").copy(orderIndex = 3, deleted = true),
        )
        val template = RoadmapTemplates.fromItems(items)
        assertEquals(listOf(TemplateItem("약분", "교과서 3단원", "EBS", 0), TemplateItem("통분", "", "", 7), TemplateItem("분수의 덧셈", "", "", null)), template)
        assertEquals(template, RoadmapTemplates.decode(RoadmapTemplates.encode(template))) // 저장했다 되살려도 같음
        assertTrue(RoadmapTemplates.decode("").isEmpty())
        val start = LocalDate.of(2029, 9, 1)
        val copied = RoadmapTemplates.toItems(template, start, "math2", byName = "", byRole = "MENTOR")
        assertEquals(listOf(start.toEpochDay(), start.plusDays(7).toEpochDay(), null), copied.map { it.targetDate })
        assertEquals(listOf(0, 1, 2), copied.map { it.orderIndex }); assertEquals(setOf(RoadmapStatus.PLANNED), copied.map { it.status }.toSet()) // 상태는 옮기지 않음
        assertEquals(setOf("math2"), copied.map { it.subjectId }.toSet()); assertEquals("MENTOR", copied[0].createdByRole)
    }
}
