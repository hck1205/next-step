package com.nextstep.app.domain.mentor

import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.Role
import java.time.LocalDate

/** 멘토가 낸 과제(작성자 역할이 MENTOR)를 모아 완료율·밀린 것·곧 마감을 봅니다. */
object AssignmentStats {
    private const val SOON_DAYS = 7L
    /** 목록마다 보여 줄 최대 개수(지금 기준 3개 규칙보다 조금 넓게, 섹션 화면이므로). */
    const val LIST_LIMIT = 5

    fun isAssignment(task: TaskEntity): Boolean = !task.deleted && task.createdByRole == Role.MENTOR.name

    fun report(tasks: List<TaskEntity>, subjects: List<SubjectEntity>, today: LocalDate): AssignmentReport {
        val mine = tasks.filter(::isAssignment)
        val todayDay = today.toEpochDay()
        val overdue = mine.filter { !it.done && it.dueDate < todayDay }.sortedBy { it.dueDate }
        val soon = mine.filter { !it.done && it.dueDate in todayDay..todayDay + SOON_DAYS }.sortedBy { it.dueDate }
        val bySubject = mine.groupBy { it.subjectId }.map { (id, list) -> AssignmentSubject(subjects.firstOrNull { it.id == id }, list.count { it.done }, list.size) }
            .sortedWith(compareBy<AssignmentSubject> { it.subject == null }.thenBy { if (it.total == 0) 1.0 else it.done.toDouble() / it.total })
        val done = mine.count { it.done }
        return AssignmentReport(
            total = mine.size,
            done = done,
            overdue = overdue.take(LIST_LIMIT),
            dueSoon = soon.take(LIST_LIMIT),
            recentDone = mine.filter { it.done }.sortedByDescending { it.dueDate }.take(LIST_LIMIT),
            bySubject = bySubject,
            line = when {
                mine.isEmpty() -> "아직 낸 과제가 없어요. + 버튼으로 과제를 내면 여기서 챙겨요."
                overdue.isNotEmpty() -> "밀린 과제 ${overdue.size}개부터 같이 챙겨요."
                soon.isNotEmpty() -> "이번 주 마감 ${soon.size}개, 밀린 과제는 없어요."
                else -> "낸 과제 ${mine.size}개 중 ${done}개 끝냈어요."
            },
        )
    }
}
