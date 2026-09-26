package com.nextstep.app.domain.goaltree

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.Role
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 할 일과 목표의 기록: 주별 달성률, 누가 준 할 일인지 · 과목별 달성률, 시간순 타임라인. 순수 함수입니다.
 * 달성률의 분모는 "그 기간에 마감이던 할 일", 분자는 그중 끝낸 것입니다. 비교는 아이 자신의 지난 기록과만 합니다.
 */
object PlanHistory {
    const val WEEKS = 8
    const val WINDOW_DAYS = 28L
    const val TIMELINE_LIMIT = 40

    fun weeks(tasks: List<TaskEntity>, today: LocalDate, count: Int = WEEKS): List<WeekRate> {
        val thisWeek = today.with(DayOfWeek.MONDAY)
        val live = tasks.filter { !it.deleted }
        return (count - 1 downTo 0).map { back ->
            val start = thisWeek.minusWeeks(back.toLong())
            val inWeek = live.filter { it.dueDate in start.toEpochDay() until start.plusWeeks(1).toEpochDay() }
            WeekRate(start, inWeek.size, inWeek.count { it.done })
        }
    }

    /** 최근 [WINDOW_DAYS]일(오늘까지 마감) 할 일을 준 사람별로: 스스로 · 학부모가 · 멘토가. */
    fun byAssigner(tasks: List<TaskEntity>, today: LocalDate): List<RateBy> {
        val recent = window(tasks, today)
        return Role.entries.mapNotNull { role ->
            val mine = recent.filter { it.createdByRole == role.name }
            if (mine.isEmpty()) null else RateBy(role.name, assignerLabel(role), mine.count { it.done }, mine.size)
        }
    }

    /** 최근 [WINDOW_DAYS]일 과목별(과목 없는 할 일은 "과목 밖"). */
    fun bySubject(tasks: List<TaskEntity>, subjects: List<SubjectEntity>, today: LocalDate): List<RateBy> {
        val recent = window(tasks, today)
        val names = subjects.associate { it.id to it.name }
        return recent.groupBy { it.subjectId?.takeIf { id -> id in names } }
            .map { (id, list) -> RateBy(id ?: "", id?.let { names.getValue(it) } ?: "과목 밖", list.count { it.done }, list.size) }
            .sortedByDescending { it.total }
    }

    /** 최근 [WINDOW_DAYS]일 전체 달성률. 마감이던 할 일이 없으면 null. */
    fun recentRate(tasks: List<TaskEntity>, today: LocalDate): Float? {
        val recent = window(tasks, today)
        return if (recent.isEmpty()) null else recent.count { it.done }.toFloat() / recent.size
    }

    /** 시간순 기록(최근 먼저): 목표 시작 · 할 일 끝(끝낸 시각이 있는 것) · 목표 달성. */
    fun timeline(goals: List<GoalEntity>, tasks: List<TaskEntity>, zone: ZoneId = ZoneId.systemDefault(), limit: Int = TIMELINE_LIMIT): List<HistoryEvent> {
        val tree = GoalTree.treeGoals(goals)
        val byId = goals.associateBy { it.id }
        val day = { millis: Long -> Instant.ofEpochMilli(millis).atZone(zone).toLocalDate() }
        val started = tree.map { HistoryEvent(day(it.createdAt), HistoryKind.GOAL_STARTED, it.title, it.createdByRole) }
        val achieved = tree.filter { it.status == GoalStatus.DONE && it.doneAt != null }.map {
            HistoryEvent(day(it.doneAt!!), HistoryKind.GOAL_ACHIEVED, it.title, it.createdByRole, leadsToTitle = it.leadsTo?.let { id -> byId[id]?.title })
        }
        val done = tasks.filter { !it.deleted && it.done && it.doneAt != null }.map {
            HistoryEvent(day(it.doneAt!!), HistoryKind.TASK_DONE, it.title, it.createdByRole, goalTitle = it.goalId?.let { id -> byId[id]?.title })
        }
        return (started + achieved + done).sortedWith(compareByDescending<HistoryEvent> { it.date }.thenByDescending { it.kind.ordinal }).take(limit)
    }

    fun assignerLabel(role: Role): String = when (role) {
        Role.STUDENT -> "스스로"
        Role.PARENT -> "학부모가"
        Role.MENTOR -> "멘토가"
    }

    private fun window(tasks: List<TaskEntity>, today: LocalDate): List<TaskEntity> {
        val from = today.minusDays(WINDOW_DAYS).toEpochDay()
        return tasks.filter { !it.deleted && it.dueDate in from..today.toEpochDay() }
    }
}
