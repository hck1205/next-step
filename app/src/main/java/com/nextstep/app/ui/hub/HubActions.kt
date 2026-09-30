package com.nextstep.app.ui.hub

/** 기록 탭 밖으로 나가는 내비게이션 콜백. 탭 안의 섹션끼리 오가는 것은 허브가 직접 처리합니다. */
data class HubActions(
    val onOpenSubject: (String) -> Unit = { _ -> },
    val onOpenJourney: () -> Unit = {},
    /** 교육 프로젝트 한 개 화면(목표 id). */
    val onOpenProject: (String) -> Unit = { _ -> },
    /** 목표 트리의 목표 한 개 화면(목표 id). */
    val onOpenGoal: (String) -> Unit = { _ -> },
)
