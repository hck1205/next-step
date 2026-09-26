package com.nextstep.app.domain.gamify

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.local.entity.ProjectLogEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.WeekPlanEntity

/** 게임 요소를 계산할 기록 묶음. ViewModel 이 스트림을 모아 한 번에 넘깁니다. */
data class GameInputs(
    val tasks: List<TaskEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val steps: List<GoalStepEntity> = emptyList(),
    val logs: List<ProjectLogEntity> = emptyList(),
    val plans: List<WeekPlanEntity> = emptyList(),
    val sessions: List<StudySessionEntity> = emptyList(),
)
