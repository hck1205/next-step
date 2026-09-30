package com.nextstep.app.domain.journey

/** 여정 이정표의 종류. 필터와 색상 구분에 씁니다. [familyOnly] 는 가족의 일이라 멘토에게 보이지 않는 종류입니다.
 * 행정·등록(상담 주간, 고입·수시 원서)은 멘토도 알아야 할 일정이라 가족 전용이 아닙니다. */
enum class MilestoneCategory(val label: String, val familyOnly: Boolean = false) {
    ADMIN("행정·등록"),
    HEALTH("건강·검진", familyOnly = true),
    LANGUAGE("언어"),
    LEARNING("학습"),
    SOCIAL("경험·사회성"),
    CAREER("진로"),
    FINANCE("재정·지원", familyOnly = true);

    companion object {
        fun from(value: String?): MilestoneCategory = entries.firstOrNull { it.name == value } ?: LEARNING
    }
}
