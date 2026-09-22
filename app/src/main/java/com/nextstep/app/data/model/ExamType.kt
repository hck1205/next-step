package com.nextstep.app.data.model

enum class ExamType(val label: String) {
    MIDTERM("중간고사"),
    FINAL("기말고사"),
    QUIZ("쪽지시험"),
    MOCK("모의고사"),
    ASSIGNMENT("수행평가");

    companion object {
        fun from(value: String?): ExamType = entries.firstOrNull { it.name == value } ?: QUIZ
    }
}
