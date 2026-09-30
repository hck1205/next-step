package com.nextstep.app.domain.taskboard

import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.live
import com.nextstep.app.domain.goaltree.PlanHistory
import com.nextstep.app.domain.task.doneOn
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.ZoneId

/**
 * 할 일 보드: 과목별 줄로 정리합니다. 과목마다 밀린 것 → 오늘 → 다가오는 것, 그 과목의 추천,
 * 이번 주 끝낸 수와 최근 4주 달성률. 과목 없는 할 일(목표·생활)은 맨 뒤 한 줄. 순수 함수입니다.
 */
object TaskBoard {
    const val SUGGESTIONS_PER_LANE = 3

    fun lanes(tasks: List<TaskEntity>, subjects: List<SubjectEntity>, suggestions: List<TaskSuggestion>, today: LocalDate, zone: ZoneId = DateUtils.zone): List<SubjectLane> {
        val live = tasks.live()
        val known = subjects.map { it.id }.toSet()
        val keyOf = { id: String? -> id?.takeIf { it in known } }
        val subjectLanes = subjects.sortedBy { it.orderIndex }.map { s -> lane(s, live.filter { keyOf(it.subjectId) == s.id }, suggestions.filter { keyOf(it.subjectId) == s.id }, today, zone) }
        val other = lane(null, live.filter { keyOf(it.subjectId) == null }, suggestions.filter { keyOf(it.subjectId) == null }, today, zone)
        return (subjectLanes.sortedWith(compareByDescending<SubjectLane> { it.overdue.size }.thenByDescending { it.pendingCount }) + other).filterNot { it.isEmpty }
    }

    private fun lane(subject: SubjectEntity?, tasks: List<TaskEntity>, suggestions: List<TaskSuggestion>, today: LocalDate, zone: ZoneId): SubjectLane {
        val day = today.toEpochDay()
        val open = tasks.filter { !it.done }.sortedBy { it.dueDate }
        val monday = DateUtils.weekStart(today)
        return SubjectLane(
            subject = subject,
            overdue = open.filter { it.dueDate < day }, today = open.filter { it.dueDate == day }, upcoming = open.filter { it.dueDate > day },
            doneThisWeek = tasks.count { t -> t.done && t.doneOn(zone)?.let { !it.isBefore(monday) } ?: (t.dueDate >= monday.toEpochDay() && t.dueDate <= day) },
            suggestions = suggestions.take(SUGGESTIONS_PER_LANE),
            recentRate = PlanHistory.recentRate(tasks, today),
        )
    }
}
