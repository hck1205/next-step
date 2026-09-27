package com.nextstep.app.domain.today

import com.nextstep.app.domain.hub.Concern

/** 멘토 오늘 화면의 카드. 이 순서가 곧 화면 순서이고, [concern] 으로 묶입니다. */
enum class MentorTodayCard(val title: String, val concern: Concern, val shortcut: Boolean = false) {
    STAGE("이 시기의 기준", Concern.OVERVIEW),
    INSIGHTS("분석", Concern.OVERVIEW),
    SUBJECTS("담당 과목", Concern.OVERVIEW),
    TASKS("내가 낸 과제", Concern.PLAN),
    STATS("이번 주 요약", Concern.STUDY),
    WEEK_CHART("과목별 학습 시간", Concern.STUDY),
    PROGRESS("진도·복습률", Concern.STUDY),
    ROADMAP("학습 로드맵", Concern.LEARN),
    /** 바로가기: 카드가 아니라 머리의 ⋮ 메뉴 항목. */
    CONTENT("콘텐츠 저장소", Concern.LEARN, shortcut = true),
    GRADES("최근 성적", Concern.EXAMS),
}
