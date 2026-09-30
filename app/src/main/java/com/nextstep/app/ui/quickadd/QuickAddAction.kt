package com.nextstep.app.ui.quickadd

import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.growth.StudentHomeSection
import com.nextstep.app.domain.growth.StudentUiLevel

/** 기록하기 시트의 항목. 역할별로 5개 이하만 보입니다. */
enum class QuickAddAction(val title: String, val subtitle: String) {
    ACTIVITY("활동 기록", "현장학습·취미·동아리 · 종류, 제목, 날짜면 끝"),
    TASK("할 일 하나", "이번 학기 목표 단계에서 고르거나 직접"),
    GRADE("성적 입력", "시험 이름, 점수, 반 평균(선택)"),
    EVENT("일정 추가", "학원·시험·체험 일정"),
    TIMER("타이머", "공부 시작을 기록해요");

    companion object {
        /**
         * 권한에 따라 보이는 항목. 순서가 곧 화면 순서이고, 가장 자주 쓰는 것이 맨 위입니다.
         * 학생은 타이머 → 활동 → 할 일 순이고 화면 단계만큼만(씨앗: 활동, 새싹: 타이머·활동, 떡잎: + 할 일, 타이머는 그 단계에 있을 때만).
         * 어른(학부모·멘토)은 할 일 주기·과제 내기가 맨 위입니다.
         */
        fun availableFor(caps: Capabilities, level: StudentUiLevel? = null): List<QuickAddAction> {
            val order = if (caps.isStudent) listOf(TIMER, ACTIVITY, TASK, GRADE, EVENT) else listOf(TASK, ACTIVITY, GRADE, EVENT)
            return order.filter { allowed(it, caps, level) }.take(minOf(MAX_ITEMS, level?.recordChoices ?: MAX_ITEMS))
        }

        private fun allowed(action: QuickAddAction, caps: Capabilities, level: StudentUiLevel?): Boolean = when (action) {
            TIMER -> caps.canUseTimer && level?.shows(StudentHomeSection.TIMER) != false
            ACTIVITY -> caps.canRecordActivities
            TASK -> caps.canCreateTasks
            GRADE -> caps.canEditGrades
            EVENT -> caps.canEditEvents
        }

        const val MAX_ITEMS = 5
    }
}
