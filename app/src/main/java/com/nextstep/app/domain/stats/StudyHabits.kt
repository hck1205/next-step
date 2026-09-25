package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.domain.time.DateUtils
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.roundToInt

/** 공부 기록에서 습관(언제·얼마나 오래·며칠 이어서)을 읽습니다. 학생·학부모·멘토가 같은 계산을 봅니다. */
object StudyHabits {
    /** 습관을 읽는 기간(4주). */
    const val WINDOW_DAYS = 28
    private const val WEEK = 7L
    /** 한 번에 이만큼 넘게 앉아 있으면 쉬어 가라고 알려 줍니다. */
    const val LONG_SESSION_MINUTES = 90
    private const val PERCENT = 100

    fun report(sessions: List<StudySessionEntity>, today: LocalDate, days: Int = WINDOW_DAYS): StudyHabitReport {
        val from = today.minusDays(days - 1L)
        val inWindow = sessions.filter { !it.deleted && DateUtils.toLocalDate(it.startAt).let { d -> !d.isBefore(from) && !d.isAfter(today) } }
        val perDay = inWindow.groupBy { DateUtils.toLocalDate(it.startAt) }.mapValues { (_, l) -> l.sumOf { it.durationMinutes } }
        val byPart = DayPart.entries.associateWith { part -> inWindow.filter { DayPart.of(DateUtils.toLocalDateTime(it.startAt).hour) == part }.sumOf { it.durationMinutes } }
        val byWeekday = DayOfWeek.entries.associateWith { wd -> perDay.filterKeys { it.dayOfWeek == wd }.values.sum() }
        val total = inWindow.sumOf { it.durationMinutes }
        val studied = { d: LocalDate -> (perDay[d] ?: 0) > 0 }
        val thisWeek = minutes(perDay, today.minusDays(WEEK - 1), today)
        val lastWeek = minutes(perDay, today.minusDays(2 * WEEK - 1), today.minusDays(WEEK))
        val timer = inWindow.filter { it.fromTimer }.sumOf { it.durationMinutes }
        val base = StudyHabitReport(
            days = days,
            totalMinutes = total,
            activeDays = perDay.count { it.value > 0 },
            sessionCount = inWindow.size,
            averageSessionMinutes = if (inWindow.isEmpty()) 0 else (total.toDouble() / inWindow.size).roundToInt(),
            longestSessionMinutes = inWindow.maxOfOrNull { it.durationMinutes } ?: 0,
            byPart = byPart,
            byWeekday = byWeekday,
            bestPart = byPart.filterValues { it > 0 }.maxByOrNull { it.value }?.key,
            bestWeekday = byWeekday.filterValues { it > 0 }.maxByOrNull { it.value }?.key,
            currentStreak = currentStreak(today, studied),
            longestStreak = longestStreak(from, today, studied),
            thisWeekMinutes = thisWeek,
            lastWeekMinutes = lastWeek,
            timerPercent = if (total == 0) 0 else timer * PERCENT / total,
            lines = emptyList(),
        )
        return base.copy(lines = lines(base))
    }

    /** 오늘 아직 안 했으면 어제부터 셉니다(오늘 하루가 끝나기 전에 연속이 끊긴 것처럼 보이지 않게). */
    fun currentStreak(today: LocalDate, studied: (LocalDate) -> Boolean): Int {
        var day = if (studied(today)) today else today.minusDays(1)
        var count = 0
        while (studied(day)) { count++; day = day.minusDays(1) }
        return count
    }

    fun longestStreak(from: LocalDate, to: LocalDate, studied: (LocalDate) -> Boolean): Int {
        var best = 0; var run = 0; var day = from
        while (!day.isAfter(to)) { run = if (studied(day)) run + 1 else 0; best = maxOf(best, run); day = day.plusDays(1) }
        return best
    }

    /** 맨 위 문장: 가장 잘 되는 때 → 한 번의 길이 → 이어 가기 → 지난주와 비교. */
    fun lines(r: StudyHabitReport): List<String> {
        if (r.isEmpty) return listOf("최근 ${r.days / WEEK}주 동안 공부 기록이 없어요. 타이머로 한 번 재 보면 습관이 보여요.")
        return listOfNotNull(
            r.bestPart?.let { "${it.label}에 가장 많이 공부해요(${DateUtils.formatMinutes(r.byPart.getValue(it))})." },
            when {
                r.longestSessionMinutes > LONG_SESSION_MINUTES -> "한 번에 평균 ${r.averageSessionMinutes}분, 가장 길게는 ${r.longestSessionMinutes}분 앉아 있었어요. 중간에 쉬어 가요."
                else -> "한 번에 평균 ${r.averageSessionMinutes}분씩 공부해요."
            },
            "${r.days}일 중 ${r.activeDays}일 공부했어요" + if (r.currentStreak > 1) " · 지금 ${r.currentStreak}일째 이어 가는 중." else ".",
            when {
                r.lastWeekMinutes == 0 && r.thisWeekMinutes == 0 -> null
                r.lastWeekMinutes == 0 -> "지난주보다 더 했어요."
                else -> {
                    val change = (r.thisWeekMinutes - r.lastWeekMinutes) * PERCENT / r.lastWeekMinutes
                    when {
                        change >= STEADY_PERCENT -> "지난 7일은 그 전 7일보다 ${change}% 더 했어요."
                        change <= -STEADY_PERCENT -> "지난 7일은 그 전 7일보다 ${-change}% 줄었어요."
                        else -> "지난주와 비슷하게 꾸준해요."
                    }
                }
            },
        )
    }

    private const val STEADY_PERCENT = 15

    private fun minutes(perDay: Map<LocalDate, Int>, from: LocalDate, to: LocalDate): Int =
        perDay.filterKeys { !it.isBefore(from) && !it.isAfter(to) }.values.sum()
}
