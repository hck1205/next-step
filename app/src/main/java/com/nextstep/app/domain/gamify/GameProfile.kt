package com.nextstep.app.domain.gamify

/**
 * 아이의 게임 요소 한 벌: 경험치 · 레벨 · 내역, 연속 기록, 배지, 이번 주 도전, 스티커판.
 * [style] 은 나이에 맞춘 모양이고, 화면은 이 값을 보고 스티커판 · 레벨 · 성장 기록 중 하나로 그립니다.
 */
data class GameProfile(
    val xp: Int,
    val level: GameLevel,
    val lines: List<XpLine>,
    val stats: GameStats,
    val badges: List<BadgeProgress>,
    val challenges: List<WeekChallenge>,
    val style: GameStyle = GameStyle.LEVELS,
    /** 이번 주에 붙인 스티커 수(스티커판 모양의 한 판). */
    val stickersThisWeek: Int = 0,
) {
    val earnedBadges: List<BadgeProgress> get() = badges.filter { it.earned }
    /** 곧 받을 배지(가장 가까운 것부터). */
    val nextBadges: List<BadgeProgress> get() = badges.filter { !it.earned }.sortedByDescending { it.ratio }
    val challengesDone: Int get() = challenges.count { it.complete }
    val boards: Int get() = stats.boards

    companion object {
        val EMPTY = GameProfile(0, GameLevel.level(1), emptyList(), GameStats(), emptyList(), emptyList())
    }
}
