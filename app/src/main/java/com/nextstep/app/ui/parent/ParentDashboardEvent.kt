package com.nextstep.app.ui.parent

import androidx.lifecycle.ViewModel
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.project.ProjectProgress
import com.nextstep.app.domain.project.RoutineItem
import java.time.LocalDate

/** ParentDashboard 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface ParentDashboardEvent {
    data class AssignTask(val title: String, val subjectId: String?, val type: TaskType, val due: LocalDate, val createdByRole: String) : ParentDashboardEvent
    /** 오늘의 루틴 한 줄 체크(다시 누르면 취소). 어린 아이의 루틴은 부모가 함께 하고 체크합니다. */
    data class ToggleRoutine(val progress: ProjectProgress, val item: RoutineItem) : ParentDashboardEvent
    /** 이번 주 계획: 어른이 맡은 단계면 쓰기 · 체크 · 같이 돌아보기, 아이가 먼저 계획하는 단계면 확인. */
    data class SaveWeekPlan(val goals: List<String>, val minutes: Int) : ParentDashboardEvent
    data class ToggleWeekGoal(val planId: String, val index: Int) : ParentDashboardEvent
    data class ApproveWeek(val planId: String) : ParentDashboardEvent
    /** 받을 차례가 된 보상을 줬다고 남깁니다. */
    data class GiveReward(val id: String) : ParentDashboardEvent
    data class ReflectWeek(val week: LocalDate, val mood: Int, val good: String, val hard: String, val change: String) : ParentDashboardEvent
}
