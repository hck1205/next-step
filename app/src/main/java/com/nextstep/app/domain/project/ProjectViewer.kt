package com.nextstep.app.domain.project

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.model.Role

/**
 * 교육 프로젝트를 보는 사람. 프로젝트는 준 쪽과 학생만 봅니다([ProjectKind]).
 * - 학생: 가족의 모든 프로젝트(스스로 만든 것 + 받은 것).
 * - 학부모: 학부모가 준 프로젝트 모두(보호자 두 사람이 함께 챙깁니다).
 * - 멘토: 내가 준 프로젝트만(다른 멘토의 것은 보지 않음). 만든 사람의 id 가 없는 옛 기록은 멘토 모두가 봅니다.
 * 학생이 스스로 만든 프로젝트는 학생만 봅니다. 누가 만들었는지 모르는 기록은 모두 봅니다.
 * 프로젝트가 아닌 목표에는 제한이 없습니다.
 */
data class ProjectViewer(val role: Role?, val memberId: String?) {
    fun canSee(goal: GoalEntity): Boolean {
        if (!ProjectPlanner.isProject(goal) || role == null || role == Role.STUDENT) return true
        val kind = ProjectKind.of(goal) ?: return true
        return when (kind) {
            ProjectKind.SELF -> false
            ProjectKind.PARENT -> role == Role.PARENT
            ProjectKind.MENTOR -> role == Role.MENTOR && (goal.createdById.isBlank() || goal.createdById == memberId)
        }
    }
}
