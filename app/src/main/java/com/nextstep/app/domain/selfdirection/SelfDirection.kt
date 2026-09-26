package com.nextstep.app.domain.selfdirection

import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.domain.stats.BalanceStats
import com.nextstep.app.domain.time.DateUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

/**
 * 자기주도 사다리 계산. 순수 함수입니다.
 * - 단계: 학부모가 고른 값(MemberEntity.selfDirection)이 있으면 그것, 없으면 학생 화면 단계의 기본값.
 * - 한 주: 월요일 시작. 금~일에는 이번 주를, 월~목에는 아직 안 돌아본 지난주를 돌아봅니다.
 * - 준비 신호: 최근 4주(이번 주 제외)의 흔적으로 한 칸 맡길지, 잠깐 같이 할지 제안합니다.
 */
object SelfDirection {
    const val MAX_GOALS = 3
    /** 흔적을 보는 주 수. */
    const val WINDOW_WEEKS = 4
    /** 주간 계획에서 고르는 공부 시간(분). */
    val MINUTE_CHOICES = listOf(60, 120, 180, 240, 300, 420, 600)

    fun defaultStage(student: MemberEntity?, today: LocalDate): SelfDirectionStage =
        SelfDirectionStage.defaultFor(student?.let { StudentUiLevel.of(it, today) } ?: StudentUiLevel.TREE)

    fun stageOf(student: MemberEntity?, today: LocalDate): SelfDirectionStage =
        SelfDirectionStage.fromName(student?.selfDirection) ?: defaultStage(student, today)

    fun planFor(plans: List<WeekPlanEntity>, weekStart: LocalDate): WeekPlanEntity? =
        plans.firstOrNull { !it.deleted && it.weekStart == weekStart.toEpochDay() }

    /** 그 주(월~일)에 공부한 분. */
    fun minutesIn(sessions: List<StudySessionEntity>, weekStart: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Int {
        val end = weekStart.plusDays(DAYS_IN_WEEK)
        return sessions.filter { !it.deleted }.filter {
            val day = DateUtils.toLocalDate(it.startAt, zone)
            !day.isBefore(weekStart) && day.isBefore(end)
        }.sumOf { it.durationMinutes }
    }

    /** 돌아볼 주: 금~일이면 이번 주, 월~목이면 지난주(계획이 있었고 아직 안 돌아봤을 때만). 이번 주를 이미 돌아봤으면 null. */
    fun reflectWeek(plans: List<WeekPlanEntity>, today: LocalDate): LocalDate? {
        val thisWeek = DateUtils.weekStart(today)
        if (today.dayOfWeek >= DayOfWeek.FRIDAY) return thisWeek.takeIf { planFor(plans, it)?.isReflected != true }
        val last = thisWeek.minusWeeks(1)
        return last.takeIf { planFor(plans, it)?.let { p -> p.hasPlan && !p.isReflected } == true }
    }

    fun week(stage: SelfDirectionStage, plans: List<WeekPlanEntity>, sessions: List<StudySessionEntity>, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): WeekStatus {
        val start = DateUtils.weekStart(today)
        val last = listOf(start, start.minusWeeks(1)).firstNotNullOfOrNull { w -> planFor(plans, w)?.takeIf { it.isReflected } }
        return WeekStatus(stage, start, planFor(plans, start), minutesIn(sessions, start, zone), reflectWeek(plans, today), last)
    }

    /** 이번 주를 뺀 최근 [WINDOW_WEEKS]주의 흔적(오래된 주 → 최근 주). */
    fun evidence(plans: List<WeekPlanEntity>, sessions: List<StudySessionEntity>, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): List<WeekEvidence> {
        val thisWeek = DateUtils.weekStart(today)
        return (WINDOW_WEEKS downTo 1).map { back ->
            val w = thisWeek.minusWeeks(back.toLong())
            val p = planFor(plans, w)
            WeekEvidence(
                weekStart = w, planned = p?.hasPlan == true, childPlanned = p?.hasPlan == true && p.authorRole == Role.STUDENT.name,
                reflected = p?.isReflected == true, goalsDone = p?.doneCount ?: 0, goalsTotal = p?.goalList?.size ?: 0,
                keptRatio = p?.plannedMinutes?.takeIf { it > 0 }?.let { minutesIn(sessions, w, zone).toFloat() / it },
            )
        }
    }

    fun report(
        student: MemberEntity?, plans: List<WeekPlanEntity>, sessions: List<StudySessionEntity>, tasks: List<TaskEntity>, today: LocalDate,
        zone: ZoneId = ZoneId.systemDefault(),
    ): SelfDirectionReport {
        val stage = stageOf(student, today)
        val weeks = evidence(plans, sessions, today, zone)
        val selfRatio = BalanceStats.selfDirectedRatio(tasks, today)
        return SelfDirectionReport(stage, defaultStage(student, today), weeks, selfRatio, suggest(stage, weeks, selfRatio))
    }

    /**
     * 준비 신호(최근 4주 중 3주 = [ENOUGH_WEEKS]).
     * - 올리기: 따라 해요·골라요는 돌아보기를 꾸준히(+ 골라요는 스스로 만든 할 일 20% 이상), 같이 계획해요부터는 아이가 쓴 계획이 꾸준하고
     *   먼저 계획해요는 계획의 70%, 스스로 해요는 80% 이상을 지켰을 때.
     * - 잠깐 같이 하기: 아이가 맡은 단계인데 4주 중 3주 이상 계획이 없거나, 계획한 주의 절반도 못 지킨 주가 3주 이상일 때.
     */
    fun suggest(stage: SelfDirectionStage, weeks: List<WeekEvidence>, selfTaskRatio: Float?): StageSuggestion? {
        val reflected = weeks.count { it.reflected }
        val childPlanned = weeks.count { it.childPlanned }
        val kept = weeks.mapNotNull { it.keptRatio }
        val keptWeeks = { min: Float -> kept.count { it >= min } }
        val missedWeeks = kept.count { it < LOW_KEPT }
        val prev = stage.previous
        if (stage.owner(LoopStep.PLAN) == Owner.CHILD && prev != null) {
            if (weeks.count { !it.planned } >= ENOUGH_WEEKS) return StageSuggestion(prev, up = false, "최근 ${weeks.size}주 중 ${weeks.count { !it.planned }}주 계획이 없었어요. 잠깐 같이 계획해 볼까요?")
            if (missedWeeks >= ENOUGH_WEEKS) return StageSuggestion(prev, up = false, "계획의 절반도 못 한 주가 많았어요. 계획을 작게 같이 세워 볼까요?")
        }
        val next = stage.next ?: return null
        val ready = when (stage) {
            SelfDirectionStage.FOLLOW -> reflected >= ENOUGH_WEEKS
            SelfDirectionStage.CHOOSE -> reflected >= ENOUGH_WEEKS && (selfTaskRatio ?: 0f) >= SELF_TASK_READY
            SelfDirectionStage.PLAN_TOGETHER -> childPlanned >= ENOUGH_WEEKS
            SelfDirectionStage.PLAN_FIRST -> childPlanned >= ENOUGH_WEEKS && keptWeeks(KEPT_READY) >= ENOUGH_WEEKS
            SelfDirectionStage.SELF -> childPlanned >= ENOUGH_WEEKS && reflected >= ENOUGH_WEEKS && keptWeeks(KEPT_SOLID) >= ENOUGH_WEEKS
            SelfDirectionStage.OWN -> false
        }
        if (!ready) return null
        val why = when (stage) {
            SelfDirectionStage.FOLLOW, SelfDirectionStage.CHOOSE -> "최근 ${weeks.size}주 중 ${reflected}주를 꾸준히 돌아봤어요."
            SelfDirectionStage.PLAN_TOGETHER -> "최근 ${weeks.size}주 중 ${childPlanned}주 계획을 아이가 직접 썼어요."
            else -> "최근 ${weeks.size}주 중 ${childPlanned}주 스스로 계획하고 대부분 지켰어요."
        }
        return StageSuggestion(next, up = true, "$why ${stage.handOverNext}")
    }

    /** 목표 줄 정리: 앞뒤 공백 제거, 빈 줄 제거, [MAX_GOALS]개까지. */
    fun cleanGoals(goals: List<String>): List<String> = goals.map { it.trim() }.filter { it.isNotEmpty() }.take(MAX_GOALS)

    private const val DAYS_IN_WEEK = 7L
    private const val ENOUGH_WEEKS = 3
    private const val SELF_TASK_READY = 0.2f
    private const val KEPT_READY = 0.7f
    private const val KEPT_SOLID = 0.8f
    private const val LOW_KEPT = 0.5f
}
