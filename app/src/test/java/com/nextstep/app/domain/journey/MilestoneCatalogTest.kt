package com.nextstep.app.domain.journey

import com.nextstep.app.domain.growth.GrowthStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MilestoneCatalogTest {
    @Test
    fun idsAreUniqueAndEveryStageHasItems() {
        val ids = MilestoneCatalog.templates.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        GrowthStage.entries.forEach { stage -> assertTrue("no items for $stage", MilestoneCatalog.forStage(stage).isNotEmpty()) }
        assertEquals(MilestoneCatalog.templates.size, MilestoneCatalog.byId.size)
    }

    @Test
    fun everyTemplateHasTextsAndSaneLeadAndPriority() {
        MilestoneCatalog.templates.forEach { t ->
            assertTrue(t.id, t.title.isNotBlank() && t.description.isNotBlank() && t.why.isNotBlank())
            assertTrue(t.id, t.leadMonths >= 0); assertTrue(t.id, t.priority in 1..3)
        }
    }

    @Test
    fun dueDatesAreOrderedRoughlyByStageForOneChild() {
        val born = LocalDate.of(2024, 5, 15)
        val byStage = GrowthStage.entries.map { stage -> MilestoneCatalog.forStage(stage).minOf { it.due.dueDate(born) } }
        assertEquals(byStage, byStage.sorted())
        // 대표 항목: 어린이집 대기는 생후 2개월, 영어 노출은 만 3세 6개월, 유치원 지원은 만 3세 되는 해 11월, 수능 원서는 고3 8월
        assertEquals(LocalDate.of(2024, 7, 15), MilestoneCatalog.byId.getValue("daycare-waitlist").due.dueDate(born))
        assertEquals(LocalDate.of(2027, 11, 15), MilestoneCatalog.byId.getValue("english-exposure").due.dueDate(born))
        assertEquals(LocalDate.of(2027, 11, 1), MilestoneCatalog.byId.getValue("kindergarten-apply").due.dueDate(born))
        assertEquals(LocalDate.of(2042, 8, 25), MilestoneCatalog.byId.getValue("csat-register").due.dueDate(born))
    }

    @Test
    fun keyMilestonesFromTheBriefExist() {
        assertNotNull(MilestoneCatalog.byId["daycare-waitlist"])
        assertNotNull(MilestoneCatalog.byId["english-exposure"])
        assertEquals(MilestoneCategory.LANGUAGE, MilestoneCatalog.byId.getValue("english-exposure").category)
        assertEquals(GrowthStage.PRESCHOOL, MilestoneCatalog.byId.getValue("english-exposure").stage)
        assertEquals(1, MilestoneCatalog.byId.getValue("daycare-waitlist").priority)
        // 과열을 거르는 이정표가 가장 어린 나이부터 이어짐: 영아 전집 → 유아 수업 수 → 영어유치원 시험 → 7세 고시
        assertEquals(
            listOf(GrowthStage.NEWBORN, GrowthStage.TODDLER, GrowthStage.PRESCHOOL, GrowthStage.PRESCHOOL),
            listOf("early-kit-pass", "class-limit-toddler", "english-kinder-check", "reading-over-cramming").map { MilestoneCatalog.byId.getValue(it).stage },
        )
        assertTrue(MilestoneCatalog.byId.getValue("english-kinder-check").description.contains("레벨테스트"))
    }
}
