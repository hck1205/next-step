package com.nextstep.app.domain.project

/** 프로젝트 표([LearningProjects]·[ActivityProjects])의 루틴 한 줄: 무엇을 · 어떤 종류 · 몇 분 · 주 며칠. */
internal object ProjectRows {
    fun r(name: String, kind: RoutineKind, minutes: Int, daysPerWeek: Int) = RoutineItem(name, kind, minutes, daysPerWeek)
}
