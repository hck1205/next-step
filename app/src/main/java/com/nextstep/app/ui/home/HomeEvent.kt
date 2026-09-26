package com.nextstep.app.ui.home

import com.nextstep.app.domain.growth.StudyKind
import androidx.lifecycle.ViewModel
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.planner.PlanOptions
import com.nextstep.app.domain.project.ProjectProgress
import com.nextstep.app.domain.project.RoutineItem

/** Home 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface HomeEvent {
    data class MarkContentWatched(val id: String) : HomeEvent
    data class GeneratePlan(val options: PlanOptions) : HomeEvent
    data object DismissPlanResult : HomeEvent
    data class SetRoadmapStatus(val id: String, val status: RoadmapStatus) : HomeEvent
    data class ToggleTask(val task: TaskEntity) : HomeEvent
    data class MarkTopic(val topic: TopicEntity, val status: TopicStatus) : HomeEvent
    data class AddQuickTask(val subject: SubjectEntity, val topic: TopicEntity, val type: TaskType) : HomeEvent
    /** "새 화면" 카드 닫기: 지금 단계를 확인한 것으로 남깁니다. */
    data object DismissLevelUp : HomeEvent
    /** "올해의 공부"의 한 가지를 오늘 할 일로. */
    data class AddStudyKind(val kind: StudyKind) : HomeEvent
    /** 오늘의 루틴 한 줄 체크(다시 누르면 취소). */
    data class ToggleRoutine(val progress: ProjectProgress, val item: RoutineItem) : HomeEvent
}
