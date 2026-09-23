package com.nextstep.app.ui.quickadd

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.ExamType
import com.nextstep.app.data.model.TaskType
import java.time.LocalDate
import java.time.LocalTime

/** 기록하기 시트의 사용자 의도. 각 항목은 필수 입력 3개 이하의 대화상자 하나로 끝납니다. */
sealed interface QuickAddEvent {
    data class Cheer(val text: String) : QuickAddEvent
    data class SaveActivity(val activity: ActivityEntity) : QuickAddEvent
    data class SaveTask(val title: String, val subjectId: String?, val type: TaskType, val due: LocalDate, val createdByRole: String) : QuickAddEvent
    data class SaveGrade(val subjectId: String, val title: String, val examType: ExamType, val score: Double, val maxScore: Double, val classAverage: Double?, val date: LocalDate, val memo: String) : QuickAddEvent
    data class SaveEvent(val title: String, val subjectId: String?, val type: EventType, val date: LocalDate, val start: LocalTime, val end: LocalTime, val repeatWeekly: Boolean, val location: String, val memo: String) : QuickAddEvent
    data object ClearMessage : QuickAddEvent
}
