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
    /** 나의 공부 달력(5주)과 최근 7일 막대 — 남과 견주지 않고 내 기록만. 숫자를 보는 나이부터. */
    STUDY_FLOW("나의 공부 흐름", Concern.STUDY),
    /** 이번 주 한마디: 부모·멘토와 같은 사실을 학생의 말로(격려와 다음 한 걸음). 숫자를 보는 나이부터. */
    FEEDBACK("이번 주 한마디", Concern.OVERVIEW),
    ROUTINE("오늘의 루틴", Concern.PROJECT),
    /** 레벨·이번 주 도전·배지. 학부모가 게임 요소를 끄면(MemberEntity.gamify) 보이지 않습니다. */
    GAME("나의 레벨", Concern.PLAN),
    WEEK("이번 주 별", Concern.STUDY),
    YEAR("올해의 공부", Concern.LEARN),
    EVENTS("오늘 일정", Concern.STUDY),
    /** 가족 일정(가족 달력): 오늘 것과 미리 보기에 든 다가오는 것. */
    FAMILY("가족 일정", Concern.FAMILY),
    REVIEW("다시 보기", Concern.LEARN),
    RECOMMENDATION("추천 영상", Concern.LEARN),
    PREVIEW("미리 보기", Concern.LEARN),
    MISSION("시험·목표", Concern.EXAMS),
    EXAM("다가오는 시험", Concern.EXAMS),
    /** 과목별 내 점수 흐름(같은 눈금). 성적이 본격적으로 쌓이는 중학생부터. */
    MY_SCORES("나의 점수 흐름", Concern.EXAMS),
    CURRICULUM("이번 학기 배울 것", Concern.LEARN),
    SUBJECTS("과목별 진도", Concern.STUDY),
    ROADMAP("멘토 로드맵", Concern.LEARN),
    JOURNEY("여정", Concern.PLAN),
    PLANNER("학습 계획 만들기", Concern.PLAN, shortcut = true);

    companion object {
        /** "전체"에서 먼저 펼칠 카드: 오늘 할 일. */
        val FOCUS = listOf(TASKS)
    }
}
