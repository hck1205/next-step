package com.nextstep.app.domain.gamify

/**
 * 배지: 해낸 일의 개수·꾸준함이 [target] 에 닿으면 받습니다. 결과(점수)나 남과의 비교로 주는 배지는 없습니다.
 * 이름은 저장하지 않고 매번 기록에서 계산하므로, 기기가 바뀌어도 같습니다.
 */
enum class Badge(val label: String, val how: String, val target: Int, private val measure: (GameStats) -> Int) {
    FIRST_TASK("첫 할 일", "할 일을 처음 끝내요", 1, { it.tasksDone }),
    TASKS_10("할 일 10개", "할 일 10개를 끝내요", 10, { it.tasksDone }),
    TASKS_50("할 일 50개", "할 일 50개를 끝내요", 50, { it.tasksDone }),
    SELF_5("스스로 시작", "스스로 정한 일 5개를 끝내요", 5, { it.selfDone }),
    SELF_30("스스로 30", "스스로 정한 일 30개를 끝내요", 30, { it.selfDone }),
    ON_TIME_10("마감 지킴이", "마감 전에 10번 끝내요", 10, { it.onTime }),
    STREAK_3("3일 연속", "3일 이어서 무언가 해요", 3, { it.bestStreak }),
    STREAK_7("7일 연속", "일주일 이어서 무언가 해요", 7, { it.bestStreak }),
    STREAK_30("30일 연속", "한 달 이어서 무언가 해요", 30, { it.bestStreak }),
    FIRST_GOAL("첫 목표", "목표를 처음 이뤄요", 1, { it.goals }),
    GOALS_5("목표 5개", "목표 5개를 이뤄요", 5, { it.goals }),
    ROUTINE_20("루틴 20번", "교육 프로젝트 루틴을 20번 해요", 20, { it.routines }),
    PHASE_1("단계 통과", "교육 프로젝트 단계를 처음 통과해요", 1, { it.phases }),
    PLANNER_4("계획 4주", "주간 계획을 4주 세워요", 4, { it.weekPlans }),
    REFLECT_4("돌아보기 4주", "한 주를 4번 돌아봐요", 4, { it.reflections }),
    STUDY_10H("공부 10시간", "타이머로 10시간을 채워요", MINUTES_10H, { it.studyMinutes });

    fun progressOf(stats: GameStats): Int = measure(stats)
    fun earned(stats: GameStats): Boolean = measure(stats) >= target
}

private const val MINUTES_10H = 600
