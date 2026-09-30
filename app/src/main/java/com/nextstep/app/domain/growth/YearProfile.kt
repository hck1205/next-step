package com.nextstep.app.domain.growth

import com.nextstep.app.domain.planner.PlanOptions

/**
 * 한 해(만 나이 또는 학년)의 공부 모양. 해마다 배우는 것과 양이 다르므로 학생 화면은 이 값으로 매년 달라집니다.
 *
 * - [kinds]: 그 해의 공부 종류와 권장 양("올해의 공부" 카드)
 * - [dailyMinutes]: 학교 밖 하루 권장 학습(분). 0 이면 앉아서 하는 공부를 권하지 않는 해. 균형 판단의 권장선이 됩니다
 * - [sessionMinutes]·[sessionsPerDay]: 타이머·학습 계획의 한 번 길이와 횟수
 * - [textScale]·[taskRows]: 해마다 조금씩 작아지는 글씨와 늘어나는 할 일 줄 수
 * - [lead]: 오늘 화면에서 타이머 다음에 먼저 보여 줄 카드(예: 중2 첫 지필이면 시험·목표)
 */
data class YearProfile(
    val key: String,
    val label: String,
    val level: StudentUiLevel,
    val theme: String,
    val subjects: List<String>,
    val kinds: List<StudyKind>,
    val dailyMinutes: Int,
    val sessionMinutes: Int,
    val sessionsPerDay: Int,
    val studyDaysPerWeek: Int,
    val textScale: Float,
    val taskRows: Int,
    val lead: List<StudentHomeSection>,
) {
    /** 주간 권장선(분). */
    val weekMinutes: Int get() = dailyMinutes * studyDaysPerWeek

    /** 학습 계획 기본값: 시작 시각·쉬는 시간은 [base] 를 따르고 길이·횟수·주말만 이 해의 값으로. */
    fun planOptions(base: PlanOptions): PlanOptions = base.copy(
        sessionMinutes = sessionMinutes,
        sessionsPerDay = sessionsPerDay,
        includeWeekend = studyDaysPerWeek > WEEKDAYS,
    )

    private companion object {
        const val WEEKDAYS = 5
    }
}
