package com.nextstep.app.ui.home

import androidx.lifecycle.ViewModel
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.planner.PlanOptions

/** Home 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface HomeEvent {
    data class MarkContentWatched(val id: String) : HomeEvent
    data class GeneratePlan(val options: PlanOptions) : HomeEvent
    data object DismissPlanResult : HomeEvent
    data class SetRoadmapStatus(val id: String, val status: RoadmapStatus) : HomeEvent
    data class ToggleTask(val task: TaskEntity) : HomeEvent
    data class MarkTopic(val topic: TopicEntity, val status: TopicStatus) : HomeEvent
    data class AddQuickTask(val subject: SubjectEntity, val topic: TopicEntity, val type: TaskType) : HomeEvent
}
