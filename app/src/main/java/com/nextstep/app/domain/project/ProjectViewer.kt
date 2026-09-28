package com.nextstep.app.domain.project

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.model.Role

/**
 * 교육 프로젝트를 보는 사람. 프로젝트는 만든 사람과 학생만 봅니다([ProjectKind]).
 * - 학생: 가족의 모든 프로젝트(스스로 만든 것 + 받은 것).
 * - 학부모·멘토: 내가 준 프로젝트만. 학생이 스스로 만든 프로젝트는 보지 않습니다.
 * 만든 사람의 id 가 없는 옛 기록은 같은 역할이 만든 것으로 봅니다. 누가 만들었는지 모르는 기록은 모두 봅니다.
 * 프로젝트가 아닌 목표에는 제한이 없습니다.
 */
data class ProjectViewer(val role: Role?, val memberId: String?) {
    fun canSee(goal: GoalEntity): Boolean {
        if (!ProjectPlanner.isProject(goal) || role == null || role == Role.STUDENT) return true
        val kind = ProjectKind.of(goal) ?: return true
        if (kind == ProjectKind.SELF) return false
        if (goal.createdById.isNotBlank()) return goal.createdById == memberId
        return kind.assigner.role == role
    }
}
