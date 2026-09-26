package com.nextstep.app.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.domain.stats.ReviewItem
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.family.StudentContext
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.domain.selfdirection.SelfDirection
import com.nextstep.app.domain.stats.ReviewPlanner
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.taskboard.TaskBoard
import com.nextstep.app.domain.taskboard.TaskSuggester
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 기록 › 목표·할 일 › 할 일. 할 일을 과목별로 정리하고, 흩어진 근거(복습 목록 · 로드맵 · 다가오는 시험)에서 나온 추천을
 * 그 과목 줄 아래에 붙입니다. 추천은 눌러서 할 일로(목표에 넣기도) 바꿉니다.
 */
class TodoViewModel(
    streams: FamilyDataStreams,
    private val tasks: TaskRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val filter = MutableStateFlow(TodoFilter.ALL)

    private val sources = combine(streams.topics, streams.subjects, streams.grades, streams.roadmap, streams.events) { topics, subjects, grades, roadmap, events ->
        Sources(ReviewPlanner.plan(topics, subjects, grades), roadmap, events, subjects)
    }

    val state: StateFlow<TodoUiState> = combine(sources, streams.tasks, streams.goals, streams.members, filter) { src, all, goals, members, f ->
        val day = today()
        val suggestions = TaskSuggester.suggest(src.review, src.roadmap, StudyStats.upcomingExams(src.events, all), src.subjects, all, day)
        TodoUiState(
            loaded = true, lanes = TaskBoard.lanes(all, src.subjects, suggestions, day),
            goals = GoalTree.treeGoals(goals).filter { it.status == GoalStatus.ACTIVE }, filter = f,
            stage = SelfDirection.stageOf(StudentContext.of(members, day).student, day),
        )
    }.asUiState(viewModelScope, TodoUiState())

    fun onEvent(event: TodoEvent) {
        when (event) {
            is TodoEvent.SetFilter -> filter.value = event.filter
            is TodoEvent.Toggle -> viewModelScope.launch { tasks.setDone(event.taskId, event.done) }
            is TodoEvent.Accept -> viewModelScope.launch {
                val s = event.suggestion
                val goal = state.value.goals.firstOrNull { it.id == event.goalId }
                tasks.save(
                    TaskEntity(
                        familyId = "", subjectId = s.subjectId, topicId = s.topicId, title = s.title, type = s.type, dueDate = s.due.toEpochDay(),
                        createdByRole = event.createdByRole, note = listOfNotNull(goal?.title, s.source.label).joinToString(" · "), goalId = goal?.id,
                    ),
                )
            }
        }
    }

    private data class Sources(
        val review: List<ReviewItem>,
        val roadmap: List<RoadmapItemEntity>,
        val events: List<EventEntity>,
        val subjects: List<SubjectEntity>,
    )
}
