package com.nextstep.app.ui.quickadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.ExamType
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.repository.ActivityRepository
import com.nextstep.app.data.repository.EventRepository
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.GradeRepository
import com.nextstep.app.data.repository.NoteRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.time.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import com.nextstep.app.ui.common.asUiState

/** 모든 쓰기의 단일 입구. 각 저장소에 한 번 쓰고 한 줄 메시지를 남깁니다. */
class QuickAddViewModel(
    streams: FamilyDataStreams,
    private val notes: NoteRepository,
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

    fun cheer(text: String) = viewModelScope.launch {
        if (text.isBlank()) return@launch
        notes.add(text.trim()); message.value = "격려를 남겼어요"
    }

    fun saveActivity(activity: ActivityEntity) = viewModelScope.launch { activities.save(activity); message.value = "활동을 기록했어요" }

    fun saveTask(title: String, subjectId: String?, type: TaskType, due: LocalDate, createdByRole: String) = viewModelScope.launch {
        if (title.isBlank()) return@launch
        tasks.save(TaskEntity(familyId = "", subjectId = subjectId, title = title.trim(), type = type, dueDate = due.toEpochDay(), createdByRole = createdByRole))
        message.value = "할 일을 추가했어요"
    }

    fun saveGrade(subjectId: String, title: String, examType: ExamType, score: Double, maxScore: Double, classAverage: Double?, date: LocalDate, memo: String) = viewModelScope.launch {
        grades.save(GradeEntity(familyId = "", subjectId = subjectId, title = title, examType = examType, score = score, maxScore = maxScore, classAverage = classAverage, date = date.toEpochDay(), memo = memo))
        message.value = "성적을 입력했어요"
    }

    fun saveEvent(title: String, subjectId: String?, type: EventType, date: LocalDate, start: LocalTime, end: LocalTime, repeatWeekly: Boolean, location: String, memo: String) = viewModelScope.launch {
        val startMs = DateUtils.toMillis(date, start)
        val endMs = DateUtils.toMillis(date, if (end.isAfter(start)) end else start.plusHours(1))
        events.save(EventEntity(familyId = "", subjectId = subjectId, title = title, type = type, startAt = startMs, endAt = endMs, repeatWeekly = repeatWeekly, location = location, memo = memo))
        message.value = "일정을 추가했어요"
    }

    fun clearMessage() { message.value = null }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: QuickAddEvent) {
        when (event) {
            is QuickAddEvent.Cheer -> cheer(event.text)
            is QuickAddEvent.SaveActivity -> saveActivity(event.activity)
            is QuickAddEvent.SaveTask -> saveTask(event.title, event.subjectId, event.type, event.due, event.createdByRole)
            is QuickAddEvent.SaveGrade -> saveGrade(event.subjectId, event.title, event.examType, event.score, event.maxScore, event.classAverage, event.date, event.memo)
            is QuickAddEvent.SaveEvent -> saveEvent(event.title, event.subjectId, event.type, event.date, event.start, event.end, event.repeatWeekly, event.location, event.memo)
            QuickAddEvent.ClearMessage -> clearMessage()
        }
    }
}
