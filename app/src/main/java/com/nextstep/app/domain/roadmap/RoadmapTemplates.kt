package com.nextstep.app.domain.roadmap

import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.live
import java.time.LocalDate

/**
 * 로드맵 템플릿(튜터 Pro 로 나눌 수 있는 기능 — 지금은 모두 열림): 한 학생의 로드맵을 순서·간격 그대로 저장해 다른 학생에게 복사합니다.
 * 날짜는 첫 목표일에서 며칠째인지로 저장해, 불러올 때 새 시작일에 맞춰 다시 놓습니다. 상태·완료는 옮기지 않습니다(새 학생은 처음부터).
 */
object RoadmapTemplates {

    fun fromItems(items: List<RoadmapItemEntity>): List<TemplateItem> {
        val live = items.live().sortedWith(compareBy<RoadmapItemEntity>({ it.orderIndex }, { it.targetDate ?: Long.MAX_VALUE }))
        val first = live.mapNotNull { it.targetDate }.minOrNull()
        return live.map { TemplateItem(it.title, it.description, it.resource, it.targetDate?.let { d -> first?.let { (d - it).toInt() } }) }
    }

    /** [start] 부터 다시 놓은 새 로드맵 항목(가족 id·작성자는 저장소가 채움). */
    fun toItems(template: List<TemplateItem>, start: LocalDate, subjectId: String?, byName: String, byRole: String): List<RoadmapItemEntity> =
        template.mapIndexed { i, t ->
            RoadmapItemEntity(
                familyId = "", subjectId = subjectId, title = t.title, description = t.description, resource = t.resource,
                targetDate = t.dayOffset?.let { start.plusDays(it.toLong()).toEpochDay() }, orderIndex = i, createdByName = byName, createdByRole = byRole,
            )
        }

    /** 저장용 글(한 줄에 한 항목, 칸은 보이지 않는 구분자). */
    fun encode(items: List<TemplateItem>): String = items.joinToString(ROW) { listOf(it.title, it.description, it.resource, it.dayOffset?.toString().orEmpty()).joinToString(FIELD) }

    fun decode(text: String): List<TemplateItem> = if (text.isEmpty()) emptyList() else text.split(ROW).mapNotNull { row ->
        val f = row.split(FIELD)
        f.getOrNull(0)?.takeIf { it.isNotBlank() }?.let { TemplateItem(it, f.getOrElse(1) { "" }, f.getOrElse(2) { "" }, f.getOrNull(3)?.toIntOrNull()) }
    }

    private const val ROW = "\u001E"
    private const val FIELD = "\u001F"
}
