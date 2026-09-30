package com.nextstep.app.domain.report

import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.domain.feedback.Finding
import com.nextstep.app.domain.stats.SubjectProgress

/**
 * 멘토 리포트의 재료: 담당 과목으로 이미 좁힌 과목 · 공부 기록 · 진도 · 할 일(멘토 과제 + 과목 없는 것) · 이번 주 피드백 사실.
 * 멘토 화면이 한 번 모아 주간·월간 리포트([LessonReports.week] · [LessonReports.month])에 같이 넘깁니다.
 */
data class MentorStudy(
    val studentName: String,
    val mentorName: String,
    val subjects: List<SubjectEntity>,
    val sessions: List<StudySessionEntity>,
    val progress: List<SubjectProgress>,
    val tasks: List<TaskEntity>,
    val findings: List<Finding>,
)
