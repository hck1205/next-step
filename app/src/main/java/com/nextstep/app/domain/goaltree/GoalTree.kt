package com.nextstep.app.domain.goaltree

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.newId
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.journey.GoalArea
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * 사람이 직접 만드는 목표 트리(학생 · 부모 · 멘토 누구나). 순수 함수입니다.
 * - 목표 = GoalEntity(trackId = [TRACK]). 기한(targetDate)은 선택이고, 시험·목표(미션)와 섞이지 않습니다.
 * - 세부 할 일 = TaskEntity(goalId). 만든 사람의 역할이 곧 "누가 준 할 일"이고, 끝낸 시각(doneAt)이 기록이 됩니다.
 * - 이어짐 = GoalEntity.leadsTo: 작은 목표를 이루면 큰 목표의 달성률이 오릅니다. 순환은 만들지 않습니다.
 */
object GoalTree {
    const val TRACK = "tree"
    /** 사람이 고르는 목표 분류(시험·수행평가·입시는 시험·목표에서). */
    val AREAS: List<GoalArea> = listOf(
        GoalArea.KOREAN, GoalArea.MATH, GoalArea.LANGUAGE, GoalArea.HABIT, GoalArea.EXPERIENCE, GoalArea.HOBBY, GoalArea.CLUB, GoalArea.CAREER, GoalArea.CUSTOM,
    )

    fun isTreeGoal(goal: GoalEntity): Boolean = goal.trackId == TRACK

    fun create(title: String, why: String, area: GoalArea, targetDate: LocalDate?, leadsTo: String?, createdByRole: String): GoalEntity = GoalEntity(
        id = newId(), familyId = "", trackId = TRACK, title = title.trim(), area = area.name, description = why.trim(),
        targetDate = targetDate?.toEpochDay(), leadsTo = leadsTo, createdByRole = createdByRole,
    )

    /** 목표의 세부 할 일. 메모에 목표 제목을 남겨 다른 할 일 목록에서도 어느 목표의 일인지 보입니다. */
    fun subTask(goal: GoalEntity, title: String, due: LocalDate, subjectId: String?, type: TaskType, createdByRole: String): TaskEntity = TaskEntity(
        familyId = "", subjectId = subjectId, title = title.trim(), type = type, dueDate = due.toEpochDay(), createdByRole = createdByRole,
        note = goal.title, goalId = goal.id,
    )

    fun treeGoals(goals: List<GoalEntity>): List<GoalEntity> = goals.filter { !it.deleted && isTreeGoal(it) }

    fun nodes(goals: List<GoalEntity>, tasks: List<TaskEntity>, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): List<GoalNode> {
        val tree = treeGoals(goals)
        return tree.map { node(it, tree, tasks, today, zone) }
    }

    fun node(goal: GoalEntity, goals: List<GoalEntity>, tasks: List<TaskEntity>, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): GoalNode {
        val tree = treeGoals(goals)
        val mine = tasks.filter { !it.deleted && it.goalId == goal.id }
            .sortedWith(compareBy<TaskEntity> { it.done }.thenBy { if (it.done) -(it.doneAt ?: 0L) else it.dueDate })
        val created = Instant.ofEpochMilli(goal.createdAt).atZone(zone).toLocalDate()
        val last = (mine.mapNotNull { it.doneAt } + listOfNotNull(goal.doneAt)).maxOrNull()?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
            ?.takeIf { it.isAfter(created) } ?: created
        return GoalNode(
            goal = goal, tasks = mine,
            children = tree.filter { it.leadsTo == goal.id && it.status != GoalStatus.ARCHIVED }.sortedBy { it.createdAt },
            chain = chain(goal, tree),
            overdue = mine.count { !it.done && it.dueDate < today.toEpochDay() },
            lastActivity = last, idleDays = ChronoUnit.DAYS.between(last, today).toInt().coerceAtLeast(0),
            daysLeft = goal.targetDate?.let { (it - today.toEpochDay()).toInt() },
        )
    }

    /** 이 목표가 이어지는 목표들(바로 위부터 맨 위까지). 끊기거나 순환이면 거기서 멈춥니다. */
    fun chain(goal: GoalEntity, goals: List<GoalEntity>): List<GoalEntity> {
        val byId = goals.filter { !it.deleted }.associateBy { it.id }
        val out = mutableListOf<GoalEntity>()
        val seen = mutableSetOf(goal.id)
        var cursor = goal.leadsTo?.let { byId[it] }
        while (cursor != null && seen.add(cursor.id)) {
            out += cursor
            cursor = cursor.leadsTo?.let { byId[it] }
        }
        return out
    }

    /** 이 목표 아래로 이어진 모든 목표 id(작은 목표, 그 작은 목표…). */
    fun descendants(goalId: String, goals: List<GoalEntity>): Set<String> {
        val out = mutableSetOf<String>()
        var frontier = setOf(goalId)
        while (frontier.isNotEmpty()) {
            frontier = goals.filter { !it.deleted && it.leadsTo in frontier && it.id !in out && it.id != goalId }.map { it.id }.toSet()
            out += frontier
        }
        return out
    }

    /** [goal] 이 이어질 수 있는 목표: 자기 자신과 자기 아래 목표(순환), 보관한 목표는 뺍니다. [goal] 이 null 이면 새 목표용. */
    fun linkTargets(goal: GoalEntity?, goals: List<GoalEntity>): List<GoalEntity> {
        val tree = treeGoals(goals).filter { it.status != GoalStatus.ARCHIVED }
        if (goal == null) return tree
        val blocked = descendants(goal.id, tree) + goal.id
        return tree.filter { it.id !in blocked }
    }

    /** 맨 위 목표(이어지는 목표가 없거나 보이지 않는 목표). */
    fun roots(nodes: List<GoalNode>): List<GoalNode> {
        val ids = nodes.map { it.goal.id }.toSet()
        return nodes.filter { it.goal.leadsTo == null || it.goal.leadsTo !in ids }
    }

    /** 어른이 먼저 챙길 진행 중인 목표: 밀린 할 일 → 달성 표시만 남음 → 오래 멈춤 → 기한 가까움 순서로 [limit]개. */
    fun focus(nodes: List<GoalNode>, limit: Int = FOCUS_LIMIT): List<GoalNode> =
        nodes.filter { it.goal.status == GoalStatus.ACTIVE }
            .sortedWith(
                compareByDescending<GoalNode> { it.overdue }.thenByDescending { it.readyToAchieve }.thenByDescending { it.idleDays }
                    .thenBy { it.daysLeft ?: Int.MAX_VALUE },
            )
            .take(limit)

    /** 달성한 목표를 기준으로, 이어지는 목표가 새로 얼마나 올라갔는지 한 줄(예: "→ 영어로 3분 말하기 60%"). */
    fun nextStepLine(node: GoalNode, nodes: List<GoalNode>): String? {
        val parent = node.parent ?: return null
        val p = nodes.firstOrNull { it.goal.id == parent.id } ?: return null
        return "→ ${parent.title} ${(p.rate * PERCENT).toInt()}%"
    }

    private const val PERCENT = 100
    private const val FOCUS_LIMIT = 3
}
