package com.nextstep.app.domain.growth

import com.nextstep.app.domain.hub.Concern

/**
 * 학생 "오늘" 화면의 카드 종류. 어떤 카드를 보여 줄지는 [StudentUiLevel] 이 정합니다.
 * [label] 은 새 단계가 열릴 때 "새로 생긴 것" 칩에 쓰입니다. [concern] 은 오늘 화면에서 묶이는 관심사(기록 탭과 같은 분류)입니다.
 * 타이머는 묶지 않고 맨 위에 둡니다. [shortcut] 은 내용 없이 다른 화면으로 가는 바로가기라 카드로 놓지 않고 오늘 화면 머리의 ⋮ 메뉴에 넣습니다.
 */
enum class StudentHomeSection(val label: String, val concern: Concern, val shortcut: Boolean = false) {
    TIMER("공부 시작 버튼", Concern.STUDY),
    TASKS("오늘 할 일", Concern.PLAN),
    MY_WEEK("나의 이번 주", Concern.STUDY),
    ROUTINE("오늘의 루틴", Concern.PROJECT),
    /** 레벨·이번 주 도전·배지. 학부모가 게임 요소를 끄면(MemberEntity.gamify) 보이지 않습니다. */
    GAME("나의 레벨", Concern.PLAN),
    WEEK("이번 주 별", Concern.STUDY),
    YEAR("올해의 공부", Concern.LEARN),
    EVENTS("오늘 일정", Concern.STUDY),
    REVIEW("다시 보기", Concern.LEARN),
    RECOMMENDATION("추천 영상", Concern.LEARN),
    PREVIEW("미리 보기", Concern.LEARN),
    MISSION("시험·목표", Concern.EXAMS),
    EXAM("다가오는 시험", Concern.EXAMS),
    CURRICULUM("이번 학기 배울 것", Concern.LEARN),
    SUBJECTS("과목별 진도", Concern.STUDY),
    ROADMAP("멘토 로드맵", Concern.LEARN),
    JOURNEY("여정", Concern.PLAN),
    PLANNER("학습 계획 만들기", Concern.PLAN, shortcut = true),
}
