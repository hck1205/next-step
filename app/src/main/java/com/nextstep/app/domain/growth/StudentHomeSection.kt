package com.nextstep.app.domain.growth

/**
 * 학생 "오늘" 화면의 카드 종류. 어떤 카드를 보여 줄지는 [StudentUiLevel] 이 정합니다.
 * [label] 은 새 단계가 열릴 때 "새로 생긴 것" 칩에 쓰입니다.
 */
enum class StudentHomeSection(val label: String) {
    TIMER("공부 시작 버튼"),
    TASKS("오늘 할 일"),
    MY_WEEK("나의 이번 주"),
    ROUTINE("오늘의 루틴"),
    /** 레벨·이번 주 도전·배지. 학부모가 게임 요소를 끄면(MemberEntity.gamify) 보이지 않습니다. */
    GAME("나의 레벨"),
    WEEK("이번 주 별"),
    YEAR("올해의 공부"),
    EVENTS("오늘 일정"),
    REVIEW("다시 보기"),
    RECOMMENDATION("추천 영상"),
    PREVIEW("미리 보기"),
    MISSION("시험·목표"),
    EXAM("다가오는 시험"),
    CURRICULUM("이번 학기 배울 것"),
    SUBJECTS("과목별 진도"),
    ROADMAP("멘토 로드맵"),
    JOURNEY("여정"),
    PLANNER("학습 계획 만들기"),
}
