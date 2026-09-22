package com.nextstep.app.domain.journey

import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.domain.growth.GrowthStage
import java.time.LocalDate

/** 타임라인 위의 이정표 하나. 카탈로그 항목이거나 사용자가 직접 추가한 항목입니다. */
data class JourneyItem(
    /** 카탈로그 항목이면 템플릿 id, 직접 추가면 null. */
    val templateId: String?,
    /** 저장된 상태 행의 id. 카탈로그 항목 중 아직 손대지 않은 것은 null. */
    val entityId: String?,
    val title: String,
    val description: String,
    val why: String,
    val category: MilestoneCategory,
    val stage: GrowthStage?,
    /** 준비를 시작할 날. */
    val startDate: LocalDate,
    val dueDate: LocalDate,
    val status: MilestoneStatus,
    val priority: Int,
    val note: String = "",
) {
    val isCustom: Boolean get() = templateId == null
    val isOpen: Boolean get() = status == MilestoneStatus.UPCOMING || status == MilestoneStatus.IN_PROGRESS

    fun phase(today: LocalDate): JourneyPhase = when {
        status == MilestoneStatus.DONE -> JourneyPhase.DONE
        status == MilestoneStatus.SKIPPED -> JourneyPhase.SKIPPED
        dueDate.isBefore(today) -> JourneyPhase.OVERDUE
        !startDate.isAfter(today) -> JourneyPhase.NOW
        else -> JourneyPhase.UPCOMING
    }
}

/** 오늘 기준 타임라인 구간. */
enum class JourneyPhase(val label: String) {
    OVERDUE("지난 항목"),
    NOW("지금 준비할 것"),
    UPCOMING("다가오는 것"),
    DONE("완료"),
    SKIPPED("건너뜀"),
}
