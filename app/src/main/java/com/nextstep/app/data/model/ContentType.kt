package com.nextstep.app.data.model

/** 교육 콘텐츠 유형. 등록 시 자동 분류되고 사용자가 수정할 수 있습니다. */
enum class ContentType(val label: String) {
    CONCEPT("개념 강의"),
    PROBLEM("문제 풀이"),
    SUMMARY("요약·정리"),
    EXAM_PREP("시험 대비"),
    STUDY_METHOD("공부법"),
    MOTIVATION("동기부여"),
    DOCUMENTARY("교양·다큐"),
    OTHER("기타");

    companion object {
        fun from(value: String?): ContentType = entries.firstOrNull { it.name == value } ?: OTHER
    }
}
