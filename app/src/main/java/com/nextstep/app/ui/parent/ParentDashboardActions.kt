package com.nextstep.app.ui.parent

import com.nextstep.app.domain.hub.ConcernSection

/** ParentDashboard 화면 밖으로 나가는 내비게이션 콜백. 기본값은 no-op 이라 프리뷰·테스트에서 생략할 수 있습니다. */
data class ParentDashboardActions(
    val onOpenSettings: () -> Unit = {},
    val onOpenSubject: (String) -> Unit = { _ -> },
    /** 기록 탭의 세그먼트로 바로 갑니다. */
    val onOpenRecords: (ConcernSection) -> Unit = { _ -> },
    val onOpenMentor: () -> Unit = {},
    val onOpenRoadmap: () -> Unit = {},
    val onOpenContent: () -> Unit = {},
    val onOpenJourney: () -> Unit = {},
    val onOpenGoals: () -> Unit = {},
    /** 다자녀: 다른 자녀로 전환. */
    val onSwitchChild: (String) -> Unit = { _ -> },
)
