package com.nextstep.app.domain.report

import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.feedback.FeedbackLine
import com.nextstep.app.domain.mentor.AssignmentReport
import com.nextstep.app.domain.stats.SubjectProgress
import java.time.LocalDate

/** 수업 리포트 재료(담당 과목으로 이미 좁힌 것). [notes] 는 같은 사실을 학부모의 말로 옮긴 이번 주 피드백. */
data class LessonReportInput(
    val studentName: String,
    val mentorName: String,
    val subjects: List<SubjectEntity>,
    val today: LocalDate,
    val weekMinutes: Int,
    val progress: List<SubjectProgress>,
    val assignments: AssignmentReport,
    val notes: List<FeedbackLine>,
)
