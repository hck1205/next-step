package com.nextstep.app.domain.gamify

/** 아이의 게임 요소 한 벌: 경험치 · 레벨 · 내역, 연속 기록, 배지, 이번 주 도전. */
data class GameProfile(
    val xp: Int,
    val level: GameLevel,
    val lines: List<XpLine>,
    val stats: GameStats,
    val badges: List<BadgeProgress>,
    val challenges: List<WeekChallenge>,
) {
    val earnedBadges: List<BadgeProgress> get() = badges.filter { it.earned }
    /** 곧 받을 배지(가장 가까운 것부터). */
    val nextBadges: List<BadgeProgress> get() = badges.filter { !it.earned }.sortedByDescending { it.ratio }
    val challengesDone: Int get() = challenges.count { it.complete }

    companion object {
        val EMPTY = GameProfile(0, GameLevel.level(1), emptyList(), GameStats(), emptyList(), emptyList())
    }
}
