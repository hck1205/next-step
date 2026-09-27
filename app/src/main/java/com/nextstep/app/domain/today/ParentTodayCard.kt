package com.nextstep.app.domain.today

import com.nextstep.app.domain.hub.Concern

/** 학부모 오늘 화면의 카드(상태 카드 아래). 이 순서가 곧 화면 순서이고, [concern] 으로 묶입니다. 묶음마다 흐름을 보여 주는 차트 카드가 앞에 옵니다. */
enum class ParentTodayCard(val title: String, val concern: Concern) {
    JOURNEY("지금 챙길 것", Concern.OVERVIEW),
    REWARDS("약속한 보상", Concern.PLAN),
    GOALS("목표 진행", Concern.PLAN),
    WEEK_RATES("주별 할 일 달성", Concern.PLAN),
    ASSIGNERS("누가 준 할 일", Concern.PLAN),
    HEAT("공부 달력 · 최근 5주", Concern.STUDY),
    DAYS("요일별 공부 시간", Concern.STUDY),
    SUBJECT_TIME("과목별 이번 주", Concern.STUDY),
    TODAY("오늘의 할 일·일정", Concern.STUDY),
    WEEK("스스로 하는 힘", Concern.STUDY),
    ROUTINE("오늘의 루틴", Concern.PROJECT),
    SCORES("과목별 점수 추이", Concern.EXAMS),
    MISSIONS("시험·목표", Concern.EXAMS),
    EXAM("다가오는 시험", Concern.EXAMS),
}
