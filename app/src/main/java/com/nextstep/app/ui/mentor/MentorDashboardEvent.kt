package com.nextstep.app.ui.mentor

import androidx.lifecycle.ViewModel
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.lesson.LessonPlan
import com.nextstep.app.domain.lesson.LessonStatus
import com.nextstep.app.domain.report.ReportKind
import java.time.LocalDate

/** MentorDashboard 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface MentorDashboardEvent {
    data class SetSubjects(val ids: List<String>) : MentorDashboardEvent
    /** [alsoTo] 는 같은 과제를 함께 받을 다른 학생들(가족 id). */
    data class AssignTask(val title: String, val subjectId: String?, val type: TaskType, val due: LocalDate, val alsoTo: List<String> = emptyList()) : MentorDashboardEvent
    /** 수업 리포트를 보냈다고 남깁니다(보낸 기록). */
    data class ReportSent(val kind: ReportKind, val title: String) : MentorDashboardEvent
    data class DeleteTask(val id: String) : MentorDashboardEvent
    /** 오늘(그날) 수업 출결. [status] 가 null 이면 지우기. */
    data class MarkLesson(val date: LocalDate, val status: LessonStatus?) : MentorDashboardEvent
    data class SaveLessonPlan(val plan: LessonPlan) : MentorDashboardEvent
}
