package com.nextstep.app.ui.lessons

import com.nextstep.app.domain.lesson.LessonPlan
import com.nextstep.app.domain.lesson.LessonStatus
import java.time.LocalDate

/** 수업·출결 화면의 사용자 의도. 적기(출결·일정)는 멘토만(caps.canKeepLessons). */
sealed interface LessonsEvent {
    data class MoveMonth(val by: Long) : LessonsEvent
    /** 그날 출결 적기. [status] 가 null 이면 기록 지우기. */
    data class Mark(val date: LocalDate, val status: LessonStatus?) : LessonsEvent
    data class SavePlan(val plan: LessonPlan) : LessonsEvent
}
