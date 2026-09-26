package com.nextstep.app.domain.reward

import com.nextstep.app.data.local.entity.RewardEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import com.nextstep.app.domain.gamify.GameLevel
import com.nextstep.app.domain.gamify.GameProfile
import com.nextstep.app.domain.gamify.GameStats
import com.nextstep.app.domain.gamify.GameStyle
import org.junit.Test

class RewardsTest {
    private fun reward(id: String, kind: RewardKind, target: String, given: Long? = null, created: Long = 1L) =
        RewardEntity(id = id, familyId = Fixtures.FAMILY, kind = kind.name, targetId = target, title = "보상$id", givenAt = given, createdAt = created)

    @Test
    fun statusComesFromTheGoalOrLevel() {
        val goals = listOf(Fixtures.goal("분수 90점", trackId = "tree", id = "g1", status = GoalStatus.DONE), Fixtures.goal("영어 일기", trackId = "tree", id = "g2"))
        val rewards = listOf(
            reward("a", RewardKind.GOAL, "g1"), reward("b", RewardKind.GOAL, "g2"), reward("c", RewardKind.LEVEL, "5"),
            reward("d", RewardKind.LEVEL, "3", given = 9L), reward("e", RewardKind.GOAL, "gone"), reward("f", RewardKind.LEVEL, "x"),
        )
        val v = Rewards.views(rewards, goals, level = 4)
        assertEquals(listOf("a" to RewardStatus.EARNED, "b" to RewardStatus.PROMISED, "c" to RewardStatus.PROMISED, "d" to RewardStatus.GIVEN), v.map { it.reward.id to it.status })
        assertEquals("분수 90점", v[0].target); assertEquals("레벨 5", v[2].target)
        assertEquals("\"분수 90점\" 이루면", v[0].condition); assertEquals("레벨 5에 닿으면", v[2].condition)
        assertEquals(listOf("a"), Rewards.due(v).map { it.reward.id })
        assertEquals(RewardStatus.EARNED, Rewards.views(rewards, goals, level = 5).first { it.reward.id == "c" }.status)
        assertEquals("b", Rewards.forGoal(v, "g2")!!.reward.id); assertNull(Rewards.forGoal(v, "none"))
    }

    @Test
    fun stickerBoardRewardsAreReachedByFilledBoards() {
        val v = Rewards.views(listOf(reward("a", RewardKind.BOARD, "2"), reward("b", RewardKind.BOARD, "3")), emptyList(), level = 1, boards = 2)
        assertEquals(listOf(RewardStatus.EARNED, RewardStatus.PROMISED), v.map { it.status })
        assertEquals("스티커판 3장", v[1].target); assertEquals("스티커판 3장 채우면", v[1].condition)
    }

    @Test
    fun whereRewardsCanBePromisedDependsOnAge() {
        assertEquals(listOf(RewardKind.BOARD, RewardKind.GOAL), Rewards.kindsFor(GameStyle.STICKERS, gameOn = true))
        assertEquals(listOf(RewardKind.GOAL, RewardKind.LEVEL), Rewards.kindsFor(GameStyle.LEVELS, gameOn = true))
        assertEquals(listOf(RewardKind.GOAL), Rewards.kindsFor(GameStyle.GROWTH, gameOn = true)) // 청소년은 목표에만
        GameStyle.entries.forEach { assertEquals(listOf(RewardKind.GOAL), Rewards.kindsFor(it, gameOn = false)) }
        val goals = listOf(Fixtures.goal("영어 일기", trackId = "tree", id = "g1"))
        val profile = GameProfile.EMPTY.copy(level = GameLevel.level(4), stats = GameStats(stickers = 25))
        assertEquals(listOf("레벨 5", "레벨 6", "레벨 7", "레벨 8", "레벨 9"), Rewards.targets(GameStyle.LEVELS, true, profile, goals).filter { it.kind == RewardKind.LEVEL }.map { it.label })
        assertEquals(listOf("3", "4", "5", "g1"), Rewards.targets(GameStyle.STICKERS, true, profile, goals).map { it.id })
        assertEquals(listOf(RewardTarget(RewardKind.GOAL, "g1", "영어 일기")), Rewards.targets(GameStyle.GROWTH, true, profile, goals))
        GameStyle.entries.forEach { assertEquals(4, Rewards.ideasFor(it).size); assertTrue(Rewards.hintFor(it).isNotBlank()) }
    }

    @Test
    fun nextRewardPrefersEarnedThenClosestLevel() {
        val goals = listOf(Fixtures.goal("분수", trackId = "tree", id = "g1"), Fixtures.goal("일기", trackId = "tree", id = "g2", status = GoalStatus.DONE))
        val promised = Rewards.views(listOf(reward("a", RewardKind.GOAL, "g1"), reward("b", RewardKind.LEVEL, "7"), reward("c", RewardKind.LEVEL, "5")), goals, level = 2)
        assertEquals("c", Rewards.next(promised)!!.reward.id)
        val withEarned = Rewards.views(listOf(reward("a", RewardKind.GOAL, "g1"), reward("c", RewardKind.LEVEL, "5"), reward("d", RewardKind.GOAL, "g2")), goals, level = 2)
        assertEquals("d", Rewards.next(withEarned)!!.reward.id)
        assertNull(Rewards.next(Rewards.views(listOf(reward("e", RewardKind.LEVEL, "2", given = 3L)), goals, level = 2)))
        assertEquals(mapOf("g1" to "보상a", "g2" to "보상d"), Rewards.openByGoal(withEarned))
    }
}
