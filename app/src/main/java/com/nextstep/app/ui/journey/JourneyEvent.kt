package com.nextstep.app.ui.journey

import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.domain.journey.MilestoneCategory
import java.time.LocalDate

/** Journey 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface JourneyEvent {
    data class SetStatus(val item: JourneyItem, val status: MilestoneStatus) : JourneyEvent
    data class SetNote(val item: JourneyItem, val note: String) : JourneyEvent
    data class SetDueDate(val item: JourneyItem, val dueDate: LocalDate) : JourneyEvent
    data class AddCustom(val title: String, val description: String, val category: MilestoneCategory, val dueDate: LocalDate, val leadMonths: Int) : JourneyEvent
    data class DeleteCustom(val item: JourneyItem) : JourneyEvent
    data class SetFilter(val category: MilestoneCategory?) : JourneyEvent
    data class ShowCompleted(val show: Boolean) : JourneyEvent
    data class ShowPast(val show: Boolean) : JourneyEvent
    data class SetBirthDate(val date: LocalDate) : JourneyEvent
    data class SetStepStatus(val step: GoalStepEntity, val status: MilestoneStatus) : JourneyEvent
    /** 단계를 이번 구간의 할 일로 보냅니다. 마감은 구간 끝. */
    data class SendStepToTasks(val step: GoalStepEntity, val createdByRole: String) : JourneyEvent
}
