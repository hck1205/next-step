package com.nextstep.app.ui.curriculum

import com.nextstep.app.domain.curriculum.CurriculumUnit

/** Curriculum 화면의 사용자 의도. */
sealed interface CurriculumEvent {
    data object PrevPeriod : CurriculumEvent
    data object NextPeriod : CurriculumEvent
    data object ThisPeriod : CurriculumEvent
    /** 과목의 미등록 단원을 내 과목·단원으로 가져옵니다. 과목이 없으면 만듭니다. */
    data class ImportSubject(val subject: String) : CurriculumEvent
    /** 단원 하나를 할 일로 보냅니다(예습·복습). */
    data class AddTask(val unit: CurriculumUnit, val createdByRole: String) : CurriculumEvent
    data class MarkWatched(val contentId: String) : CurriculumEvent
}
