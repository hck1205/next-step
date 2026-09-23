package com.nextstep.app.domain.curriculum

/**
 * 한 학기의 과목 단원 하나. [subject] 는 가족 과목명과 맞추는 표시 이름(국어·수학·영어·과학·사회·역사·한국사 등),
 * [keywords] 는 가족의 등록 단원·콘텐츠 매칭에 쓰는 힌트, [essential] 은 그 학기의 뼈대 단원(놓치면 다음 학기가 흔들리는 것).
 */
data class CurriculumUnit(
    val subject: String,
    val title: String,
    val keywords: List<String> = emptyList(),
    val essential: Boolean = false,
) {
    /** 매칭에 쓸 토큰: 제목 토큰 + 키워드. 한 글자("식", "원")는 "방정식"처럼 엉뚱한 단원에 걸리므로 뺍니다. */
    val matchTokens: List<String> get() = (title.split(Regex("[\\s·,/()]+")) + keywords).filter { it.length >= 2 }.distinct()
}

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
