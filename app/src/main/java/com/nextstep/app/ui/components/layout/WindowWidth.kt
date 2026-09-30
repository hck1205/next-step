package com.nextstep.app.ui.components.layout

/**
 * 화면 폭 단계(Material 창 크기 기준). 앱 뼈대가 여기에 맞춰 바뀝니다.
 * - [COMPACT] 폰 세로: 하단 탭 + 가운데 기록하기(+)
 * - [MEDIUM] 폴더블 펼침·태블릿 세로·폰 가로: 왼쪽 세로 탭(레일) + 맨 위 기록하기, 슬라이드 두 장씩
 * - [EXPANDED] 태블릿 가로·데스크톱: 레일 + 본문은 가운데 [MAX_CONTENT_DP] 폭까지
 */
enum class WindowWidth {
    COMPACT, MEDIUM, EXPANDED;

    /** 하단 탭 대신 왼쪽 레일을 쓰는지. */
    val usesRail: Boolean get() = this != COMPACT

    companion object {
        /** 본문 카드가 너무 넓게 늘어나지 않는 최대 폭(dp). */
        const val MAX_CONTENT_DP = 840
        private const val MEDIUM_FROM_DP = 600
        private const val EXPANDED_FROM_DP = 840

        fun of(widthDp: Int): WindowWidth = when {
            widthDp < MEDIUM_FROM_DP -> COMPACT
            widthDp < EXPANDED_FROM_DP -> MEDIUM
            else -> EXPANDED
        }
    }
}
