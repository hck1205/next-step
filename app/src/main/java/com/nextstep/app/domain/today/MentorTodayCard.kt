package com.nextstep.app.domain.today

import com.nextstep.app.domain.hub.Concern

/** 멘토 오늘 화면의 카드. 이 순서가 곧 화면 순서이고, [concern] 으로 묶입니다. 이번 주 숫자 요약은 카드가 아니라 맨 위 지표 칸(MentorKpis). */
enum class MentorTodayCard(val title: String, val concern: Concern, val shortcut: Boolean = false) {
    STAGE("이 시기의 기준", Concern.OVERVIEW),
    /** 이번 주 피드백: 담당 과목 기록으로 찾은 사실을 멘토의 말로(가족의 일은 빠짐). */
    FEEDBACK("이번 주 피드백", Concern.OVERVIEW),
    INSIGHTS("분석", Concern.OVERVIEW),
    SUBJECTS("담당 과목", Concern.OVERVIEW),
    SUBMISSIONS("과제 제출", Concern.PLAN),
    TASKS("내가 낸 과제", Concern.PLAN),
    STUDY_WEEKS("담당 과목 · 최근 8주", Concern.STUDY),
    WEEK_CHART("과목별 이번 주", Concern.STUDY),
    PROGRESS("진도·복습률", Concern.STUDY),
    ROADMAP("학습 로드맵", Concern.LEARN),
    /** 바로가기: 카드가 아니라 머리의 ⋮ 메뉴 항목. */
    CONTENT("콘텐츠 저장소", Concern.LEARN, shortcut = true),
    SCORES("점수 추이", Concern.EXAMS),
    GRADES("최근 성적", Concern.EXAMS);

    companion object {
        /** "전체"에서 먼저 펼칠 카드: 내가 낸 과제, 없으면 제출 현황. */
        val FOCUS = listOf(TASKS, SUBMISSIONS)
    }
}
