package com.nextstep.app.domain.journey

import com.nextstep.app.data.local.entity.JourneyItemEntity
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.domain.growth.GrowthStage
import java.time.LocalDate

/**
 * 생년월일 + 카탈로그 + 저장된 상태로 타임라인을 만듭니다. 순수 함수라 화면과 알림이 같은 결과를 봅니다.
 */
object JourneyPlanner {

    /**
     * @param birthDate 학생 생년월일. null 이면 카탈로그 항목은 만들 수 없고 직접 추가한 항목만 돌려줍니다.
     * @param stored 저장된 상태·직접 추가 항목.
     */
    fun build(birthDate: LocalDate?, stored: List<JourneyItemEntity>, today: LocalDate): List<JourneyItem> {
        val overrides = stored.filter { it.templateId != null && !it.deleted }.associateBy { it.templateId!! }
        val fromCatalog = if (birthDate == null) emptyList() else MilestoneCatalog.templates.map { t ->
            val saved = overrides[t.id]
            val due = saved?.dueDate?.let { LocalDate.ofEpochDay(it) } ?: t.due.dueDate(birthDate)
            JourneyItem(
                templateId = t.id, entityId = saved?.id, title = t.title, description = t.description, why = t.why,
                category = t.category, stage = t.stage, startDate = due.minusMonths(t.leadMonths.toLong()), dueDate = due,
                status = saved?.status ?: MilestoneStatus.UPCOMING, priority = t.priority, note = saved?.note ?: "",
            )
        }
        val custom = stored.filter { it.templateId == null && !it.deleted }.map { e ->
            val due = LocalDate.ofEpochDay(e.dueDate)
            JourneyItem(
                templateId = null, entityId = e.id, title = e.title, description = e.description, why = "",
                category = MilestoneCategory.from(e.category), stage = birthDate?.let { GrowthStage.fromBirthDate(it, due) },
                startDate = due.minusMonths(e.leadMonths.toLong()), dueDate = due, status = e.status, priority = e.priority, note = e.note,
            )
        }
        return (fromCatalog + custom).sortedWith(compareBy<JourneyItem> { phaseOrder(it.phase(today)) }.thenBy { it.dueDate }.thenBy { it.priority })
    }

    /** 지금 준비해야 하거나 놓친 항목. 대시보드용. */
    fun actionable(items: List<JourneyItem>, today: LocalDate, limit: Int = 3): List<JourneyItem> =
        items.filter { it.phase(today) == JourneyPhase.NOW || it.phase(today) == JourneyPhase.OVERDUE }.take(limit)

    fun completion(items: List<JourneyItem>, today: LocalDate): Float {
        val passed = items.filter { it.status == MilestoneStatus.DONE || it.status == MilestoneStatus.SKIPPED || it.dueDate.isBefore(today) }
        if (passed.isEmpty()) return 0f
        return passed.count { it.status == MilestoneStatus.DONE }.toFloat() / passed.size
    }

    private fun phaseOrder(phase: JourneyPhase): Int = when (phase) {
        JourneyPhase.OVERDUE -> 0
        JourneyPhase.NOW -> 1
        JourneyPhase.UPCOMING -> 2
        JourneyPhase.DONE -> 3
        JourneyPhase.SKIPPED -> 4
    }
}
