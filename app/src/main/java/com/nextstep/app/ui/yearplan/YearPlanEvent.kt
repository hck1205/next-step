package com.nextstep.app.ui.yearplan

import com.nextstep.app.domain.year.YearDoer
import com.nextstep.app.domain.year.YearTask

/** "올해" 탭의 사용자 의도. */
sealed interface YearPlanEvent {
    data class Toggle(val view: YearTaskView) : YearPlanEvent
    data class AddToToday(val task: YearTask) : YearPlanEvent
    /** 보는 사람의 몫(Capabilities.yearDoers). 화면이 처음 열릴 때 한 번 보냅니다. */
    data class SetMine(val doers: Set<YearDoer>) : YearPlanEvent
    /** true 면 "내 할 일"만, false 면 전체. */
    data class ShowMine(val mineOnly: Boolean) : YearPlanEvent
}
