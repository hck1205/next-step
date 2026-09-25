package com.nextstep.app.ui.yearplan

import com.nextstep.app.domain.year.YearTask

/** "올해" 탭의 사용자 의도. */
sealed interface YearPlanEvent {
    data class Toggle(val view: YearTaskView) : YearPlanEvent
    data class AddToToday(val task: YearTask) : YearPlanEvent
}
