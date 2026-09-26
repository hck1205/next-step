package com.nextstep.app.ui.goaltree

import com.nextstep.app.domain.journey.GoalArea
import java.time.LocalDate

/** 목표 화면의 사용자 의도. */
sealed interface GoalTreeEvent {
    data class SetFilter(val filter: GoalFilter) : GoalTreeEvent
    data class SetArea(val area: GoalArea?) : GoalTreeEvent
    data class Create(val title: String, val why: String, val area: GoalArea, val target: LocalDate?, val leadsTo: String?, val createdByRole: String) : GoalTreeEvent
}
