package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.domain.goaltree.PlanHistory
import com.nextstep.app.domain.mentor.AssignmentStats
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * 학부모·멘토가 흐름을 보는 차트의 값. 저장하는 값 없이 기존 기록(공부·할 일·성적)에서 계산하고, 할 일의 주별·준 사람별 달성은
 * 기록 탭과 같은 숫자가 되도록 [PlanHistory] 를 그대로 씁니다.
 * 오늘은 인자로 받아 같은 입력이면 늘 같은 결과입니다. 차트 모양은 ui/components/chart 가 정합니다.
 */
object TrendStats {

    /** 학부모·멘토 차트 값 한 벌. 멘토는 담당 과목으로 좁힌 기록을 넘깁니다. */
    fun family(sessions: List<StudySessionEntity>, tasks: List<TaskEntity>, grades: List<GradeEntity>, subjects: List<SubjectEntity>, today: LocalDate): FamilyTrends =
        FamilyTrends(
            rolling = rollingWeeks(sessions, today),
            heat = heatCalendar(sessions, today),
            daily = StudyStats.dailyMinutes(sessions, DAYS_IN_WEEK.toInt(), today),
            bySubject = StudyStats.weeklyMinutesBySubject(sessions, subjects, today).filter { it.subject != null },
            assigners = PlanHistory.byAssigner(tasks, today),
            weekRates = PlanHistory.weeks(tasks, today, RATE_WEEKS),
            scores = scoreSeries(grades, subjects),
            submissions = submissions(tasks, today),
            dailyGoal = subjects.sumOf { it.weeklyGoalMinutes } / DAYS_IN_WEEK.toInt(),
        )

    /** 오늘까지 7일씩 [periods]번의 공부 시간(오래된 것부터). 마지막 값이 최근 7일. */
    fun rollingWeeks(sessions: List<StudySessionEntity>, today: LocalDate, periods: Int = ROLLING_PERIODS): List<Int> =
        (periods - 1 downTo 0).map { k ->
            val end = today.minusDays(DAYS_IN_WEEK * k)
            StudyStats.minutesBetween(sessions, end.minusDays(DAYS_IN_WEEK - 1), end.plusDays(1))
        }

    /** 최근 [weeks]주(월~일)의 공부 달력. 이번 주의 앞날은 [HeatDay.future]. */
    fun heatCalendar(sessions: List<StudySessionEntity>, today: LocalDate, weeks: Int = HEAT_WEEKS): List<HeatWeek> {
        val first = DateUtils.weekStart(today).minusWeeks(weeks - 1L)
        return (0 until weeks).map { w ->
            val monday = first.plusWeeks(w.toLong())
            HeatWeek(monday, (0 until DAYS_IN_WEEK.toInt()).map { d ->
                val day = monday.plusDays(d.toLong())
                val future = day.isAfter(today)
                val minutes = if (future) 0 else StudyStats.minutesBetween(sessions, day, day.plusDays(1))
                HeatDay(day, minutes, heatLevel(minutes), future)
            })
        }
    }

    /** 달력 칸의 진하기: 0 안 함 · 1 15분 미만 · 2 30분 미만 · 3 45분 미만 · 4 그 이상. */
    fun heatLevel(minutes: Int): Int = when {
        minutes <= 0 -> 0
        minutes < LEVEL_STEP -> 1
        minutes < LEVEL_STEP * 2 -> 2
        minutes < LEVEL_STEP * 3 -> 3
        else -> 4
    }

    /** 과목마다 점수 흐름(시험 날짜 순, 100점 환산). 성적이 없는 과목은 빠집니다. */
    fun scoreSeries(grades: List<GradeEntity>, subjects: List<SubjectEntity>): List<ScoreSeries> = subjects.mapNotNull { s ->
        val mine = grades.filter { !it.deleted && it.subjectId == s.id }.sortedBy { it.date }
        if (mine.isEmpty()) return@mapNotNull null
        val classAvg = mine.mapNotNull { it.classPercent }
        ScoreSeries(s, mine.map { it.percent.roundToInt() }, classAvg.takeIf { it.isNotEmpty() }?.average()?.roundToInt())
    }

    /** 멘토가 낸 과제의 상태: 끝냄 · 기한 전 · 기한이 지났는데 안 함. */
    fun submissions(tasks: List<TaskEntity>, today: LocalDate): Submissions = AssignmentStats.report(tasks, emptyList(), today).submissions

    private const val DAYS_IN_WEEK = 7L
    private const val ROLLING_PERIODS = 8
    private const val HEAT_WEEKS = 5
    private const val RATE_WEEKS = 5
    private const val LEVEL_STEP = 15
}
