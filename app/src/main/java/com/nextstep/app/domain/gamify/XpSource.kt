package com.nextstep.app.domain.gamify

/**
 * 경험치가 쌓이는 곳과 한 번에 주는 양. 결과(점수·등수)가 아니라 해낸 일과 꾸준함에만 줍니다.
 * 스스로 정한 일 · 마감 지킴에는 덤을, 목표 달성 · 프로젝트 단계 통과에는 큰 몫을 둡니다.
 */
enum class XpSource(val label: String, val xp: Int) {
    TASK_DONE("할 일 끝", 2),
    ON_TIME("마감 전에 끝낸 덤", 1),
    SELF_TASK("스스로 정한 일 덤", 1),
    ROUTINE("루틴 한 번", 1),
    STUDY("공부 20분", 1),
    WEEK_PLAN("주간 계획", 3),
    REFLECTION("주간 돌아보기", 3),
    GOAL("목표 달성", 10),
    PHASE("프로젝트 단계 통과", 15),
}
