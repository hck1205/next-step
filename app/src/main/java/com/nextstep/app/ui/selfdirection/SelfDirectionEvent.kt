package com.nextstep.app.ui.selfdirection

import com.nextstep.app.domain.selfdirection.SelfDirectionStage
import java.time.LocalDate

/** 스스로 화면의 사용자 의도. */
sealed interface SelfDirectionEvent {
    data class SavePlan(val goals: List<String>, val minutes: Int) : SelfDirectionEvent
    data class ToggleGoal(val planId: String, val index: Int) : SelfDirectionEvent
    data class Approve(val planId: String) : SelfDirectionEvent
    data class Reflect(val week: LocalDate, val mood: Int, val good: String, val hard: String, val change: String) : SelfDirectionEvent
    /** 학부모가 단계를 고릅니다. null 이면 화면 단계에 맞춰 자동. */
    data class SetStage(val stage: SelfDirectionStage?) : SelfDirectionEvent
}
