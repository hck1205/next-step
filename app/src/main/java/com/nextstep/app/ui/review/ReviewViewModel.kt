package com.nextstep.app.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.data.repository.TopicRepository
import com.nextstep.app.domain.stats.ReviewItem
import com.nextstep.app.domain.stats.ReviewPlanner
import com.nextstep.app.domain.stats.ReviewReason
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 배울 것 › 복습. 목록은 ReviewPlanner 가 고르고, 여기서는 이유별로 묶어 둡니다. */
class ReviewViewModel(
    streams: FamilyDataStreams,
    private val tasks: TaskRepository,
    private val topics: TopicRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {

    val state: StateFlow<ReviewUiState> = combine(streams.topics, streams.subjects, streams.grades) { topics, subjects, grades ->
        val items = ReviewPlanner.plan(topics, subjects, grades)
        ReviewUiState(
            sections = ReviewReason.entries.mapNotNull { r -> items.filter { it.reason == r }.takeIf { it.isNotEmpty() }?.let { r to it } },
            total = items.size,
            perSubject = items.filter { it.reason != ReviewReason.NEXT_CLASS }.groupBy { it.subject.name }.map { (name, l) -> name to l.size }.sortedByDescending { it.second },
            loaded = true,
        )
    }.asUiState(viewModelScope, ReviewUiState())

    fun addTask(item: ReviewItem, byRole: String) = viewModelScope.launch {
        val preview = item.reason == ReviewReason.NEXT_CLASS
        tasks.save(
            TaskEntity(
                familyId = "", subjectId = item.subject.id, topicId = item.topic.id,
                title = "${item.subject.name} ${item.topic.title} ${if (preview) "예습" else "복습"}",
                type = if (preview) TaskType.PREVIEW else TaskType.REVIEW, dueDate = today().toEpochDay(), createdByRole = byRole,
            ),
        )
    }

    fun markDone(item: ReviewItem) = viewModelScope.launch {
        topics.setStatus(item.topic.id, if (item.reason == ReviewReason.NEXT_CLASS) TopicStatus.PREVIEWED else TopicStatus.REVIEWED)
    }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: ReviewEvent) {
        when (event) {
            is ReviewEvent.AddTask -> addTask(event.item, event.byRole)
            is ReviewEvent.MarkDone -> markDone(event.item)
        }
    }
}
