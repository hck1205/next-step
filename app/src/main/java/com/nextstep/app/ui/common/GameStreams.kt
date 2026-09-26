package com.nextstep.app.ui.common

import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.gamify.GameInputs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** 게임 요소(레벨·배지·도전)를 계산할 기록 묶음. 오늘 화면·보상 화면·학부모 첫 화면이 같이 씁니다. */
fun FamilyDataStreams.gameInputs(): Flow<GameInputs> =
    combine(tasks, goals, goalSteps, projectLogs, weekPlans) { t, g, s, l, p -> GameInputs(tasks = t, goals = g, steps = s, logs = l, plans = p) }
        .combine(sessions) { input, sessions -> input.copy(sessions = sessions) }
