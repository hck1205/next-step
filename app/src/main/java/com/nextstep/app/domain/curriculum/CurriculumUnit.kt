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
    val matchTokens: List<String> = (title.split(TOKEN_SPLIT) + keywords).filter { it.length >= 2 }.distinct()
    /** 소문자 토큰. 비교 대상만 소문자로 바꾸면 되도록. */
    val lowerTokens: List<String> = matchTokens.map { it.lowercase() }

    private companion object {
        val TOKEN_SPLIT = Regex("[\\s·,/()]+")
    }
}
