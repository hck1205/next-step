package com.nextstep.app.ui.common

import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.period.PeriodRecords
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** 기간 리포트·성장 앨범이 읽는 기록 한 벌(PeriodRecords). 월간·학기 리포트와 성장 앨범이 같이 씁니다. */
fun FamilyDataStreams.periodRecords(): Flow<PeriodRecords> =
    combine(sessions, tasks, grades, subjects) { s, t, g, sub -> PeriodRecords(sessions = s, tasks = t, grades = g, subjects = sub) }
        .combine(combine(activities, goals, cheers) { a, g, c -> Triple(a, g, c) }) { r, (a, g, c) -> r.copy(activities = a, goals = g, cheers = c) }
