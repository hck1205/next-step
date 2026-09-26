package com.nextstep.app.domain.selfdirection

import com.nextstep.app.data.local.entity.WeekPlanEntity
import java.time.LocalDate

/**
 * 오늘 화면의 "나의 이번 주": 이번 주 계획, 지금까지 공부한 분, 돌아볼 주(금~일은 이번 주, 월~목은 아직 안 돌아본 지난주).
 */
data class WeekStatus(
    val stage: SelfDirectionStage,
    val weekStart: LocalDate,
    val plan: WeekPlanEntity?,
    val actualMinutes: Int,
    /** 돌아볼 주. 없으면 null. */
    val reflectWeek: LocalDate?,
    /** 가장 최근에 돌아본 주의 기록(이번 주 또는 지난주). */
    val lastReflection: WeekPlanEntity?,
) {
    val hasPlan: Boolean get() = plan?.hasPlan == true
    val waitingApproval: Boolean get() = stage.needsApproval && hasPlan && plan?.approvedAt == null
    /** 계획한 시간 대비 한 시간(0~). 계획한 시간이 없으면 null. */
    val keptRatio: Float? get() = plan?.plannedMinutes?.takeIf { it > 0 }?.let { actualMinutes.toFloat() / it }
    val reflectsLastWeek: Boolean get() = reflectWeek != null && reflectWeek.isBefore(weekStart)
}
