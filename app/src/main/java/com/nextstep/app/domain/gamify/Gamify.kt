package com.nextstep.app.domain.gamify

import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.domain.project.ProjectPlanner
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * 게임 요소 계산. 순수 함수이며 따로 저장하는 점수는 없습니다(모든 값이 기존 기록에서 나옵니다).
 * 원칙: 해낸 일과 꾸준함에만 경험치를 주고, 점수·등수·또래 비교로는 주지 않으며, 못 한 날에 깎지 않습니다.
 * 나이에 따라 달라지는 것(도전 · 배지 · 연속 기록의 쉬는 날)은 [GameStyle] 이 정하고, 경험치 규칙은 모든 나이가 같습니다.
 */
object Gamify {
    const val STUDY_CHUNK_MINUTES = 20
    /** 이번 주 도전: 할 일 끝내기 목표의 범위. */
    const val MIN_WEEK_TASKS = 3
    const val MAX_WEEK_TASKS = 10

    fun stats(input: GameInputs, today: LocalDate, zone: ZoneId = ZoneId.systemDefault(), style: GameStyle = GameStyle.LEVELS): GameStats {
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
            stickers = stickerDays(input, zone).size,
            streak = streak(days, today, style.restDays), bestStreak = bestStreak(days, style.restDays),
        )
    }

    fun lines(s: GameStats): List<XpLine> = listOf(
        XpLine(XpSource.TASK_DONE, s.tasksDone), XpLine(XpSource.ON_TIME, s.onTime), XpLine(XpSource.SELF_TASK, s.selfDone),
        XpLine(XpSource.ROUTINE, s.routines), XpLine(XpSource.STUDY, s.studyMinutes / STUDY_CHUNK_MINUTES),
        XpLine(XpSource.WEEK_PLAN, s.weekPlans), XpLine(XpSource.REFLECTION, s.reflections),
        XpLine(XpSource.GOAL, s.goals), XpLine(XpSource.PHASE, s.phases),
    ).filter { it.count > 0 }

    fun profile(input: GameInputs, today: LocalDate, zone: ZoneId = ZoneId.systemDefault(), style: GameStyle = GameStyle.LEVELS): GameProfile {
        val s = stats(input, today, zone, style)
        val lines = lines(s)
        val xp = lines.sumOf { it.xp }
        val week = weekOf(today)
        return GameProfile(
            xp = xp, level = GameLevel.of(xp), lines = lines, stats = s,
            badges = Badge.of(style).map { BadgeProgress(it, it.progressOf(s)) },
            challenges = challenges(input, today, zone, style), style = style,
            stickersThisWeek = stickerDays(input, zone).count { it.toEpochDay() in week },
        )
    }

    /**
     * 이번 주 도전. 못 채워도 깎이는 것은 없고, 다음 주 월요일에 새로 시작합니다.
     * - 스티커판: "이번 주 스티커판 채우기" 하나.
     * - 레벨·배지: 할 일 끝내기(이번 주 마감 수에 맞춰 3~10개) · 무언가 한 날 · 한 주 돌아보기.
     * - 성장 기록: 스스로 세운 이번 주 계획 지키기(계획이 없으면 "계획 세우기") · 무언가 한 날 · 한 주 돌아보기.
     */
    fun challenges(input: GameInputs, today: LocalDate, zone: ZoneId = ZoneId.systemDefault(), style: GameStyle = GameStyle.LEVELS): List<WeekChallenge> {
        val monday = DateUtils.weekStart(today)
        val week = weekOf(today)
        if (style == GameStyle.STICKERS) {
            return listOf(WeekChallenge("이번 주 스티커판 채우기", stickerDays(input, zone).count { it.toEpochDay() in week }, GameStyle.BOARD_SIZE))
        }
        val activeThisWeek = activeDays(input, zone).count { it.toEpochDay() in week }
        val plan = input.plans.firstOrNull { !it.deleted && it.weekStart == monday.toEpochDay() }
        val reflected = plan?.isReflected == true
        val first = if (style == GameStyle.GROWTH) {
            val goals = plan?.goalList.orEmpty()
            if (goals.isEmpty()) WeekChallenge("이번 주 계획 세우기", 0, 1) else WeekChallenge("내 계획 지키기", plan!!.doneCount, goals.size)
        } else {
            val live = input.tasks.filter { !it.deleted }
            val due = live.count { it.dueDate in week }
            val done = live.count { t -> t.done && (t.doneAt?.let { DateUtils.toLocalDate(it, zone).toEpochDay() in week } ?: (t.dueDate in week)) }
            WeekChallenge("할 일 끝내기", done, due.coerceIn(MIN_WEEK_TASKS, MAX_WEEK_TASKS))
        }
        return listOf(first, WeekChallenge("무언가 한 날", activeThisWeek, style.activeDaysTarget), WeekChallenge("한 주 돌아보기", if (reflected) 1 else 0, 1))
    }

    /** 무언가 한 날: 할 일을 끝낸 날 · 루틴을 한 날 · 공부를 기록한 날. */
    fun activeDays(input: GameInputs, zone: ZoneId = ZoneId.systemDefault()): Set<LocalDate> = stickerDays(input, zone).toSet()

    /** 스티커 한 장씩의 날짜: 끝낸 할 일 · 루틴 기록 · 공부 기록 하나마다. */
    fun stickerDays(input: GameInputs, zone: ZoneId = ZoneId.systemDefault()): List<LocalDate> =
        input.tasks.filter { !it.deleted && it.done }.mapNotNull { it.doneAt?.let { m -> DateUtils.toLocalDate(m, zone) } } +
            input.logs.filter { !it.deleted }.map { LocalDate.ofEpochDay(it.date) } +
            input.sessions.filter { !it.deleted }.map { DateUtils.toLocalDate(it.startAt, zone) }

    /**
     * 오늘까지 이어진 날 수(오늘 아직이면 어제부터). [restDays] 만큼은 쉬어도 이어지고(쉰 날은 세지 않음), 그보다 길게 쉬면 끊깁니다.
     */
    fun streak(days: Set<LocalDate>, today: LocalDate, restDays: Int = 0): Int {
        var day = today
        var gap = 0
        while (day !in days) {
            gap++
            if (gap > restDays + 1) return 0
            day = day.minusDays(1)
        }
        var n = 0
        gap = 0
        while (gap <= restDays) {
            if (day in days) { n++; gap = 0 } else gap++
            day = day.minusDays(1)
        }
        return n
    }

    fun bestStreak(days: Set<LocalDate>, restDays: Int = 0): Int {
        var best = 0
        var run = 0
        var prev: LocalDate? = null
        days.sorted().forEach { d ->
            run = if (prev != null && ChronoUnit.DAYS.between(prev, d) - 1 <= restDays) run + 1 else 1
            prev = d
            best = maxOf(best, run)
        }
        return best
    }

    private fun weekOf(today: LocalDate): LongRange {
        val monday = DateUtils.weekStart(today)
        return monday.toEpochDay()..monday.plusDays(DAYS_IN_WEEK - 1).toEpochDay()
    }

    private const val DAYS_IN_WEEK = 7L
}
