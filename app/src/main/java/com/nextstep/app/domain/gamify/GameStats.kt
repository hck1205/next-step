package com.nextstep.app.domain.gamify

/** 게임 요소의 재료: 지금까지 해낸 일의 개수와 연속 기록. 모두 기존 기록(할 일·루틴·주간 계획·목표·공부 시간)에서 셉니다. */
data class GameStats(
    val tasksDone: Int = 0,
    val onTime: Int = 0,
    val selfDone: Int = 0,
    val routines: Int = 0,
    val studyMinutes: Int = 0,
    val weekPlans: Int = 0,
    val reflections: Int = 0,
    val goals: Int = 0,
    val phases: Int = 0,
    /** 오늘(오늘 아직이면 어제)까지 무언가 한 날이 이어진 수. */
    val streak: Int = 0,
    val bestStreak: Int = 0,
)
