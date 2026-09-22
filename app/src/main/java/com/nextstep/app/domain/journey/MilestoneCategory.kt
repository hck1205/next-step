package com.nextstep.app.domain.journey

/** 여정 이정표의 종류. 필터와 색상 구분에 씁니다. */
enum class MilestoneCategory(val label: String) {
    ADMIN("행정·등록"),
    HEALTH("건강·검진"),
    LANGUAGE("언어"),
    LEARNING("학습"),
    SOCIAL("경험·사회성"),
    CAREER("진로"),
    FINANCE("재정·지원");

    companion object {
        fun from(value: String?): MilestoneCategory = entries.firstOrNull { it.name == value } ?: LEARNING
    }
}
