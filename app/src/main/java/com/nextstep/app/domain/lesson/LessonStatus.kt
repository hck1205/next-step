package com.nextstep.app.domain.lesson

/** 수업 한 번의 출결. 이름은 저장값이라 바꾸지 않습니다. 아직 적지 않은 수업은 상태가 없습니다(null). */
enum class LessonStatus(val label: String) {
    DONE("출석"),
    ABSENT("결석"),
    MAKEUP("보강 필요");

    companion object {
        fun from(value: String?): LessonStatus? = entries.firstOrNull { it.name == value }
    }
}
