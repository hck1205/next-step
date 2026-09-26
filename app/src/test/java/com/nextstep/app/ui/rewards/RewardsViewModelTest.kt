package com.nextstep.app.ui.rewards

import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.gamify.Badge
import com.nextstep.app.domain.gamify.GameStyle
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.domain.reward.RewardKind
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeRewardRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class RewardsViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val rewards = FakeRewardRepository(streams)
    private val today = LocalDate.of(2029, 5, 10)
    private val noon = today.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun vm() = RewardsViewModel(streams, rewards, today = { today })

    private fun seed(gamify: Boolean = true) {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "지우", id = "kid", gradeYear = 5).copy(gamify = gamify))
        streams.goals.value = listOf(
            Fixtures.goal("분수 90점", trackId = GoalTree.TRACK, id = "g1", status = GoalStatus.DONE),
            Fixtures.goal("영어 일기", trackId = GoalTree.TRACK, id = "g2"),
            Fixtures.goal("중간고사", trackId = "exam", id = "g3"),
        )
        // 어른이 준 할 일 5개를 마감 날 끝냄: 5×2 + 마감 덤 5 = 15, 목표 달성 10 → 25 XP(레벨 2)
        streams.tasks.value = (1..5).map { Fixtures.task("t$it", today, done = true, by = "PARENT").copy(doneAt = noon) }
    }

    @Test
    fun levelBadgesAndRewardsComeFromRecords() = runTest {
        seed()
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertTrue(s.gamify); assertEquals(25, s.profile.xp); assertEquals(2, s.profile.level.number)
        assertTrue(s.profile.earnedBadges.map { it.badge }.containsAll(listOf(Badge.FIRST_TASK, Badge.FIRST_GOAL)))
        assertEquals(listOf("g2"), s.goals.map { it.id }) // 보상을 걸 수 있는 목표: 진행 중인 목표 트리만
        assertEquals(GameStyle.LEVELS, s.style) // 초5 = 레벨·배지
        assertEquals(listOf("g2", "3", "4", "5", "6", "7"), s.targets.map { it.id }); assertTrue(s.canPromise)

        vm.onEvent(RewardsEvent.Promise(RewardKind.GOAL, "g1", "보드게임"))
        vm.onEvent(RewardsEvent.Promise(RewardKind.LEVEL, "3", "영화 보기"))
        vm.onEvent(RewardsEvent.Promise(RewardKind.GOAL, "g2", "나들이"))
        s = settle(vm.state)
        assertEquals(listOf("보드게임"), s.due.map { it.reward.title })
        assertEquals(setOf("영화 보기", "나들이"), s.promised.map { it.reward.title }.toSet())
        assertEquals("보드게임", s.nextReward!!.reward.title)

        vm.onEvent(RewardsEvent.Give(s.due.single().reward.id))
        vm.onEvent(RewardsEvent.Cancel(s.promised.first { it.kind == RewardKind.LEVEL }.reward.id))
        s = settle(vm.state)
        assertEquals(listOf("보드게임"), s.given.map { it.reward.title }); assertTrue(s.due.isEmpty())
        assertEquals(listOf("나들이"), s.promised.map { it.reward.title })
        assertEquals(listOf("promise:GOAL:g1:보드게임", "promise:LEVEL:3:영화 보기", "promise:GOAL:g2:나들이"), rewards.calls.take(3))
        job.cancel()
    }

    @Test
    fun gamesOffHidesLevelsButGoalRewardsStillWork() = runTest {
        seed(gamify = false)
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertFalse(s.gamify); assertEquals(listOf(RewardKind.GOAL), s.targets.map { it.kind }.distinct()); assertTrue(s.canPromise)
        job.cancel()
    }

    @Test
    fun youngChildGetsStickerBoardsAndTeenGetsGoalOnlyRewards() = runTest {
        seed()
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "하은", id = "kid", gradeYear = 1))
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertEquals(GameStyle.STICKERS, s.style)
        assertEquals(listOf("1", "2", "3", "g2"), s.targets.map { it.id }) // 스티커판 5장(끝낸 할 일 5개 → 아직 0판) 다음 3판 + 목표
        assertEquals(listOf("이번 주 스티커판 채우기"), s.profile.challenges.map { it.label })
        vm.onEvent(RewardsEvent.Promise(RewardKind.BOARD, "1", "놀이터 30분"))
        s = settle(vm.state)
        assertEquals("스티커판 1장 채우면", s.promised.single().condition)
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "지우", id = "kid", gradeYear = 8))
        s = settle(vm.state)
        assertEquals(GameStyle.GROWTH, s.style); assertEquals(listOf("g2"), s.targets.map { it.id })
        assertTrue(s.profile.badges.none { it.badge.styles == setOf(GameStyle.STICKERS) })
        job.cancel()
    }
}
