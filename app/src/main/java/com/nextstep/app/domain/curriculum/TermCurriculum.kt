package com.nextstep.app.domain.curriculum

/** 한 학기의 커리큘럼: 단원 목록, 길러야 할 역량, 이 시기에 시작·결정할 것. */
data class TermCurriculum(
    val periodKey: String,
    val units: List<CurriculumUnit>,
    val competencies: List<String>,
    /** 이 학기에 시작하거나 결정해야 하는 것 한 줄(예: "고1 2학기 선택과목 결정"). */
    val startNow: List<String> = emptyList(),
) {
    val subjects: List<String> get() = units.map { it.subject }.distinct()
    fun unitsOf(subject: String): List<CurriculumUnit> = units.filter { it.subject == subject }
}
