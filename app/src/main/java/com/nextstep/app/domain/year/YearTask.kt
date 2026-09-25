package com.nextstep.app.domain.year

/**
 * 그 해에 학생이 해야 할 일 하나. [how] 는 한 줄 방법·분량(예: "주 1회 급수표 10문제").
 * [key] 는 분류와 제목으로 만들어 저장 상태와 연결되므로, 제목을 바꾸면 완료 표시가 풀립니다.
 */
data class YearTask(
    val area: YearArea,
    val term: YearTerm,
    val title: String,
    val how: String,
) {
    val key: String get() = "${area.name}:$title"

    /** 저장 키(여정 저장소의 templateId 로 씁니다). */
    fun storageId(yearKey: String): String = "$PREFIX$yearKey:$key"

    companion object {
        /** 여정 카탈로그 id 와 겹치지 않게 붙이는 접두어. 여정 화면은 이 행들을 무시합니다. */
        const val PREFIX = "year:"
    }
}
