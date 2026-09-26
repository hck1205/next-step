package com.nextstep.app.domain.gamify

import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class GamifyTest {
    private val zone = ZoneId.systemDefault()
    private val today = LocalDate.of(2029, 5, 10) // 목요일
    private val millis = { d: LocalDate -> d.atStartOfDay(zone).toInstant().toEpochMilli() }
    private fun done(title: String, due: LocalDate, on: LocalDate, by: String = "STUDENT") = Fixtures.task(title, due, done = true, by = by).copy(doneAt = millis(on))

    @Test
    fun levelsWidenStepByStep() {
        assertEquals(listOf(0, 25, 75, 150, 250, 375), (1..6).map { GameLevel.threshold(it) })
        assertEquals(1, GameLevel.of(0).number); assertEquals(1, GameLevel.of(24).number); assertEquals(2, GameLevel.of(25).number)
        val l = GameLevel.of(110)
        assertEquals(3, l.number); assertEquals("도전자", l.title); assertEquals(35f / 75, l.progress(110), 0.001f); assertEquals(40, l.remaining(110))
        assertEquals("전설 +2", GameLevel.level(12).title)
    }

    @Test
    fun xpComesOnlyFromEffortAndIsNeverTakenAway() {
        val input = GameInputs(
            tasks = listOf(
                done("a", today, today), // 마감 날 끝냄 → 할 일 + 마감 덤 + 스스로 덤
                done("b", today.minusDays(3), today, by = "PARENT"), // 늦게 끝냄 → 할 일만
                Fixtures.task("안 한 일", today.minusDays(5), by = "PARENT"), // 밀려도 깎이지 않음
            ),
            goals = listOf(Fixtures.goal("g", trackId = GoalTree.TRACK, status = GoalStatus.DONE), Fixtures.goal("트랙", trackId = "x", status = GoalStatus.DONE)),
            steps = listOf(GoalStepEntity(familyId = "f", goalId = "p", periodKey = "project:p1", title = "t", status = MilestoneStatus.DONE)),
            logs = listOf(Fixtures.projectLog("p", "p1", "노래", 10, today)),
            plans = listOf(WeekPlanEntity(familyId = "f", weekStart = 1L, goals = "x", reflectedAt = 1L)),
            sessions = listOf(Fixtures.session("math", today, LocalTime.of(9, 0), 45)),
        )
        val p = Gamify.profile(input, today, zone)
        val s = p.stats
        assertEquals(2, s.tasksDone); assertEquals(1, s.onTime); assertEquals(1, s.selfDone); assertEquals(1, s.goals); assertEquals(1, s.phases)
        // 2×2 + 1 + 1 + 루틴 1 + 공부 2(45분) + 계획 3 + 돌아보기 3 + 목표 10 + 단계 15
        assertEquals(40, p.xp)
        assertEquals(2, p.level.number)
        assertTrue(p.lines.none { it.count == 0 })
        assertTrue(p.earnedBadges.map { it.badge }.containsAll(listOf(Badge.FIRST_TASK, Badge.FIRST_GOAL, Badge.PHASE_1)))
        assertFalse(p.badges.first { it.badge == Badge.TASKS_10 }.earned); assertEquals(0.2f, p.badges.first { it.badge == Badge.TASKS_10 }.ratio, 0.001f)
    }

    @Test
    fun streaksCountDaysWithAnythingDone() {
        val days = setOf(today, today.minusDays(1), today.minusDays(2), today.minusDays(5), today.minusDays(6), today.minusDays(7), today.minusDays(8))
        assertEquals(3, Gamify.streak(days, today)); assertEquals(4, Gamify.bestStreak(days))
        assertEquals(4, Gamify.streak(days + today.minusDays(3), today))
        assertEquals(2, Gamify.streak(setOf(today.minusDays(1), today.minusDays(2)), today)) // 오늘 아직이면 어제까지
        assertEquals(0, Gamify.streak(emptySet(), today))
        val input = GameInputs(
            tasks = listOf(done("a", today, today)), logs = listOf(Fixtures.projectLog("p", "p1", "x", 5, today.minusDays(1))),
            sessions = listOf(Fixtures.session("math", today.minusDays(2), LocalTime.of(9, 0), 10)),
        )
        assertEquals(setOf(today, today.minusDays(1), today.minusDays(2)), Gamify.activeDays(input, zone))
    }

    @Test
    fun weeklyChallengesResetOnMonday() {
        val monday = LocalDate.of(2029, 5, 7)
        val input = GameInputs(
            tasks = listOf(done("a", monday, monday), done("b", today, today), Fixtures.task("c", today.plusDays(1)), done("지난주", monday.minusDays(2), monday.minusDays(2))),
            plans = listOf(WeekPlanEntity(familyId = "f", weekStart = monday.toEpochDay(), reflectedAt = 1L, mood = 3)),
        )
        val c = Gamify.challenges(input, today, zone)
        assertEquals(listOf("할 일 끝내기", "무언가 한 날", "한 주 돌아보기"), c.map { it.label })
        assertEquals(2, c[0].done); assertEquals(Gamify.MIN_WEEK_TASKS, c[0].target)
        assertEquals(2, c[1].done); assertEquals(Gamify.ACTIVE_DAYS_TARGET, c[1].target)
        assertTrue(c[2].complete)
    }
}
