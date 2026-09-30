package com.nextstep.app.domain.today

import com.nextstep.app.domain.hub.Concern

/** 학부모 오늘 화면의 카드(상태 카드 아래). 이 순서가 곧 화면 순서이고, [concern] 으로 묶입니다. 묶음마다 흐름을 보여 주는 차트 카드가 앞에 옵니다. */
enum class ParentTodayCard(val title: String, val concern: Concern) {
    JOURNEY("지금 챙길 것", Concern.OVERVIEW),
    /** 이번 주 피드백: 같은 사실을 학부모의 말로(FeedbackVoice) + 아이가 들은 말. */
    FEEDBACK("이번 주 피드백", Concern.OVERVIEW),
    /** 이번 주 지표 네 칸(공부 · 할 일 · 점수 · 스스로). 상태 카드 아래에 늘 펼쳐 두지 않고 한눈에 묶음 안에. */
    KPIS("이번 주 숫자", Concern.OVERVIEW),
    REWARDS("약속한 보상", Concern.PLAN),
    /** 최근 해낸 일에 응원 붙이기(아이 오늘 화면의 "받은 응원"으로). */
    CHEER("해낸 일 응원하기", Concern.PLAN),
    GOALS("목표 진행", Concern.PLAN),
    WEEK_RATES("주별 할 일 달성", Concern.PLAN),
    ASSIGNERS("누가 준 할 일", Concern.PLAN),
    /** 주말 이야기: 금~일 초대, 나눈 뒤에는 이번 주 기대되는 것. */
    TALK("주말 이야기", Concern.FAMILY),
    /** 가족 일정: 오늘 것과 미리 보기에 든 다가오는 것(가족 달력). */
    FAMILY("가족 일정", Concern.FAMILY),
    HEAT("공부 달력 · 최근 5주", Concern.STUDY),
    DAYS("요일별 공부 시간", Concern.STUDY),
    SUBJECT_TIME("과목별 이번 주", Concern.STUDY),
    TODAY("오늘의 할 일·일정", Concern.STUDY),
    WEEK("스스로 하는 힘", Concern.STUDY),
    ROUTINE("오늘의 루틴", Concern.PROJECT),
    SCORES("과목별 점수 추이", Concern.EXAMS),
    MISSIONS("시험·목표", Concern.EXAMS),
    EXAM("다가오는 시험", Concern.EXAMS);

    companion object {
        /** "전체"에서 먼저 펼칠 카드: 줄 때가 된 보상, 없으면 지금 챙길 것. */
        val FOCUS = listOf(REWARDS, JOURNEY)
    }
}
