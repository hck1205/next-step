package com.nextstep.app.data.model

/** 콘텐츠 대상 학년대. */
enum class GradeLevel(val label: String) {
    ELEMENTARY("초등"),
    MIDDLE("중등"),
    HIGH("고등"),
    ALL("전체");

    companion object {
        fun from(value: String?): GradeLevel = entries.firstOrNull { it.name == value } ?: ALL
    }
}
