package com.nextstep.app.ui.mentor

/** MentorDashboard 화면 밖으로 나가는 내비게이션 콜백. 기본값은 no-op 이라 프리뷰·테스트에서 생략할 수 있습니다. */
data class MentorDashboardActions(
    val onOpenSettings: () -> Unit = {},
    val onOpenSubject: (String) -> Unit = { _ -> },
    val onOpenRoadmap: () -> Unit = {},
    val onOpenContent: () -> Unit = {},
    val onBack: (() -> Unit)? = null,
    val onOpenJourney: () -> Unit = {},
    /** 여러 학생을 맡은 멘토: 다른 학생으로 전환. */
    val onSwitchChild: (String) -> Unit = { _ -> },
)
