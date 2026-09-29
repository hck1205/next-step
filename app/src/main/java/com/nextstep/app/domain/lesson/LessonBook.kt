package com.nextstep.app.domain.lesson

/** 멘토 한 명의 한 달 수업: 일정 · 수업 날들 · 요약 · 다음 수업료. 학부모는 멘토마다 한 권씩, 멘토는 자기 것 한 권을 봅니다. */
data class LessonBook(
    val mentorId: String,
    val mentorName: String,
    val plan: LessonPlan,
    val days: List<LessonDay>,
    val summary: LessonSummary,
    val tuition: TuitionDue?,
)
