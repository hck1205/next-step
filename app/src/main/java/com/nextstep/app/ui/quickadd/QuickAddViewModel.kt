package com.nextstep.app.ui.quickadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.repository.ActivityRepository
import com.nextstep.app.data.repository.EventRepository
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.GradeRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.entry.EventDraft
import com.nextstep.app.domain.entry.GradeDraft
import com.nextstep.app.domain.growth.KidRecord
import com.nextstep.app.domain.task.TaskDrafts
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** 모든 쓰기의 단일 입구. 각 저장소에 한 번 쓰고 한 줄 메시지를 남깁니다. */
class QuickAddViewModel(
    streams: FamilyDataStreams,
    private val activities: ActivityRepository,
    private val tasks: TaskRepository,
    private val grades: GradeRepository,
    private val events: EventRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val message = MutableStateFlow<String?>(null)

    val state: StateFlow<QuickAddUiState> = combine(streams.subjects, message) { subjects, msg ->
        QuickAddUiState(subjects = subjects, today = today(), savedMessage = msg)
    }.asUiState(viewModelScope, QuickAddUiState())

    fun saveActivity(activity: ActivityEntity) { viewModelScope.launch { activities.save(activity); message.value = "활동을 기록했어요" } }

    fun saveTask(title: String, subjectId: String?, type: TaskType, due: LocalDate, createdByRole: String) {
        viewModelScope.launch {
            if (title.isBlank()) return@launch
            tasks.save(TaskDrafts.written(title.trim(), subjectId, type, due, createdByRole))
            message.value = "할 일을 추가했어요"
        }
    }

    fun saveGrade(draft: GradeDraft) {
        viewModelScope.launch {
            grades.save(draft.toEntity())
            message.value = "성적을 입력했어요"
        }
    }

    fun saveEvent(draft: EventDraft) {
        viewModelScope.launch {
            events.save(draft.toEntity())
            message.value = "일정을 추가했어요"
        }
    }

    /** 아이용 그림 타일: 오늘 날짜의 활동 하나를 학생이 남긴 것으로 저장합니다. */
    fun kidRecord(record: KidRecord) {
        viewModelScope.launch {
            activities.save(ActivityEntity(familyId = "", type = record.type, title = record.title, date = today().toEpochDay(), createdByRole = Role.STUDENT.name))
            message.value = "${record.label}! 스티커를 받았어요"
        }
    }

    fun clearMessage() { message.value = null }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: QuickAddEvent) {
        when (event) {
            is QuickAddEvent.SaveActivity -> saveActivity(event.activity)
            is QuickAddEvent.SaveTask -> saveTask(event.title, event.subjectId, event.type, event.due, event.createdByRole)
            is QuickAddEvent.SaveGrade -> saveGrade(event.draft)
            is QuickAddEvent.SaveEvent -> saveEvent(event.draft)
            is QuickAddEvent.KidRecordTap -> kidRecord(event.record)
            QuickAddEvent.ClearMessage -> clearMessage()
        }
    }
}
