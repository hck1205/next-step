package com.nextstep.app.ui.insights

import androidx.lifecycle.ViewModel
import com.nextstep.app.domain.insight.InsightAction

/** Insights 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface InsightsEvent {
    data class ApplyAction(val action: InsightAction, val createdByRole: String) : InsightsEvent
    data class AddNote(val text: String) : InsightsEvent
    data class DeleteNote(val id: String) : InsightsEvent
}
