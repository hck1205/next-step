package com.nextstep.app.ui.grades

import androidx.lifecycle.ViewModel
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.model.ExamType
import java.time.LocalDate

/** Grades 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface GradesEvent {
    data class SetFilter(val subjectId: String?) : GradesEvent
    data class Save(val existing: GradeEntity?, val subjectId: String, val title: String, val examType: ExamType, val score: Double, val maxScore: Double, val classAverage: Double?, val date: LocalDate, val memo: String) : GradesEvent
    data class Delete(val id: String) : GradesEvent
}
