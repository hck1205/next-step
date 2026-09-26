package com.nextstep.app.domain.gamify

import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.domain.project.ProjectPlanner
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.ZoneId

/**
 * 게임 요소 계산. 순수 함수이며 따로 저장하는 점수는 없습니다(모든 값이 기존 기록에서 나옵니다).
 * 원칙: 해낸 일과 꾸준함에만 경험치를 주고, 점수·등수·또래 비교로는 주지 않으며, 못 한 날에 깎지 않습니다.
 */
object Gamify {
    const val STUDY_CHUNK_MINUTES = 20
    /** 이번 주 도전: 할 일 끝내기 목표의 범위와 "무언가 한 날" 목표. */
    const val MIN_WEEK_TASKS = 3
    const val MAX_WEEK_TASKS = 10
    const val ACTIVE_DAYS_TARGET = 4

    fun stats(input: GameInputs, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): GameStats {
        val done = input.tasks.filter { !it.deleted && it.done }
        val days = activeDays(input, zone)
        return GameStats(
            tasksDone = done.size,
            onTime = done.count { t -> t.doneAt?.let { DateUtils.toLocalDate(it, zone).toEpochDay() <= t.dueDate } == true },
            selfDone = done.count { it.isStudentMade },
            routines = input.logs.count { !it.deleted },
            studyMinutes = input.sessions.filter { !it.deleted }.sumOf { it.durationMinutes },
            weekPlans = input.plans.count { !it.deleted && it.hasPlan },
            reflections = input.plans.count { !it.deleted && it.isReflected },
            goals = GoalTree.treeGoals(input.goals).count { it.status == GoalStatus.DONE },
            phases = input.steps.count { !it.deleted && it.status == MilestoneStatus.DONE && ProjectPlanner.phaseKeyOf(it) != null },
            streak = streak(days, today), bestStreak = bestStreak(days),
        )
    }

    fun lines(s: GameStats): List<XpLine> = listOf(
        XpLine(XpSource.TASK_DONE, s.tasksDone), XpLine(XpSource.ON_TIME, s.onTime), XpLine(XpSource.SELF_TASK, s.selfDone),
        XpLine(XpSource.ROUTINE, s.routines), XpLine(XpSource.STUDY, s.studyMinutes / STUDY_CHUNK_MINUTES),
        XpLine(XpSource.WEEK_PLAN, s.weekPlans), XpLine(XpSource.REFLECTION, s.reflections),
        XpLine(XpSource.GOAL, s.goals), XpLine(XpSource.PHASE, s.phases),
    ).filter { it.count > 0 }

    fun profile(input: GameInputs, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): GameProfile {
        val s = stats(input, today, zone)
        val lines = lines(s)
        val xp = lines.sumOf { it.xp }
        return GameProfile(
            xp = xp, level = GameLevel.of(xp), lines = lines, stats = s,
            badges = Badge.entries.map { BadgeProgress(it, it.progressOf(s)) },
            challenges = challenges(input, today, zone),
        )
    }

    /**
     * 이번 주 도전 세 가지: 할 일 끝내기(이번 주 마감 수에 맞춰 3~10개) · 무언가 한 날 [ACTIVE_DAYS_TARGET]일 · 한 주 돌아보기.
     * 못 채워도 깎이는 것은 없고, 다음 주 월요일에 새로 시작합니다.
     */
    fun challenges(input: GameInputs, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): List<WeekChallenge> {
        val monday = DateUtils.weekStart(today)
        val week = monday.toEpochDay()..monday.plusDays(DAYS_IN_WEEK - 1).toEpochDay()
        val live = input.tasks.filter { !it.deleted }
        val dueThisWeek = live.count { it.dueDate in week }
        val doneThisWeek = live.count { t -> t.done && (t.doneAt?.let { DateUtils.toLocalDate(it, zone).toEpochDay() in week } ?: (t.dueDate in week)) }
        val activeThisWeek = activeDays(input, zone).count { it.toEpochDay() in week }
        val reflected = input.plans.any { !it.deleted && it.weekStart == monday.toEpochDay() && it.isReflected }
        return listOf(
            WeekChallenge("할 일 끝내기", doneThisWeek, dueThisWeek.coerceIn(MIN_WEEK_TASKS, MAX_WEEK_TASKS)),
            WeekChallenge("무언가 한 날", activeThisWeek, ACTIVE_DAYS_TARGET),
            WeekChallenge("한 주 돌아보기", if (reflected) 1 else 0, 1),
        )
    }

    /** 무언가 한 날: 할 일을 끝낸 날 · 루틴을 한 날 · 공부를 기록한 날. */
    fun activeDays(input: GameInputs, zone: ZoneId = ZoneId.systemDefault()): Set<LocalDate> =
        (input.tasks.filter { !it.deleted && it.done }.mapNotNull { it.doneAt?.let { m -> DateUtils.toLocalDate(m, zone) } } +
            input.logs.filter { !it.deleted }.map { LocalDate.ofEpochDay(it.date) } +
            input.sessions.filter { !it.deleted }.map { DateUtils.toLocalDate(it.startAt, zone) }).toSet()

    fun streak(days: Set<LocalDate>, today: LocalDate): Int {
        var day = if (today in days) today else today.minusDays(1)
        var n = 0
        while (day in days) { n++; day = day.minusDays(1) }
        return n
    }

    fun bestStreak(days: Set<LocalDate>): Int {
        var best = 0
        days.forEach { d ->
            if (d.minusDays(1) in days) return@forEach
            var n = 0
            var c = d
            while (c in days) { n++; c = c.plusDays(1) }
            best = maxOf(best, n)
        }
        return best
    }

    private const val DAYS_IN_WEEK = 7L
}
