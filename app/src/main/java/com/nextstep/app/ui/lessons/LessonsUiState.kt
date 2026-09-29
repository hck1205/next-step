package com.nextstep.app.ui.lessons

import com.nextstep.app.domain.lesson.LessonBook
import com.nextstep.app.domain.lesson.LessonPlan
import java.time.YearMonth

data class LessonsUiState(
    val month: YearMonth = YearMonth.now(),
    /** 멘토마다 이 달 수업(멘토에게는 자기 것 하나). */
    val books: List<LessonBook> = emptyList(),
    /** 멘토 본인의 수업 일정(일정 정하기 창의 처음 값). */
    val myPlan: LessonPlan = LessonPlan(emptySet(), 0, 0),
    val loaded: Boolean = false,
)
