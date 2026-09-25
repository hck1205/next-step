package com.nextstep.app.ui.quickadd

import com.nextstep.app.domain.growth.StudentHomeSection
import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.domain.access.Capabilities

/** 기록하기 시트의 항목. 역할별로 5개 이하만 보입니다. */
enum class QuickAddAction(val title: String, val subtitle: String) {
    ACTIVITY("활동 기록", "현장학습·취미·동아리 · 종류, 제목, 날짜면 끝"),
    TASK("할 일 하나", "이번 학기 목표 단계에서 고르거나 직접"),
    GRADE("성적 입력", "시험 이름, 점수, 반 평균(선택)"),
    EVENT("일정 추가", "학원·시험·체험 일정"),
    TIMER("타이머", "공부 시작을 기록해요");

    companion object {
        /**
         * 권한에 따라 보이는 항목. 순서가 곧 화면 순서입니다.
         * 학생은 화면 단계만큼만(씨앗: 활동, 새싹: 타이머·활동, 떡잎: + 할 일) 보여 줍니다. 타이머는 그 단계에 타이머가 있을 때만.
         */
        fun availableFor(caps: Capabilities, level: StudentUiLevel? = null): List<QuickAddAction> = buildList {
            if (caps.canUseTimer && level?.shows(StudentHomeSection.TIMER) != false) add(TIMER)
            if (caps.canRecordActivities) add(ACTIVITY)
            if (caps.canCreateTasks) add(TASK)
            if (caps.canEditGrades) add(GRADE)
            if (caps.canEditEvents) add(EVENT)
        }.take(minOf(MAX_ITEMS, level?.recordChoices ?: MAX_ITEMS))

        const val MAX_ITEMS = 5
    }
}
