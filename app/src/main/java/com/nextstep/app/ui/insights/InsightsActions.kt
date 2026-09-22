package com.nextstep.app.ui.insights

/** Insights 화면 밖으로 나가는 내비게이션 콜백. 기본값은 no-op 이라 프리뷰·테스트에서 생략할 수 있습니다. */
data class InsightsActions(
    val onBack: (() -> Unit)? = null,
)
