package com.nextstep.app.data.model

/** 콘텐츠 출처 범위. GLOBAL 은 운영자가 큐레이팅한 공용 저장소, FAMILY 는 이 학생의 구성원이 등록한 것. */
enum class ContentScope(val label: String) {
    GLOBAL("공용"),
    FAMILY("우리 가족");

    companion object {
        /** 저장값 → 범위. 모르는 값이면 가족 것(FAMILY)으로 봅니다. */
        fun from(value: String?): ContentScope = entries.firstOrNull { it.name == value } ?: FAMILY
    }
}
