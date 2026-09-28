package com.nextstep.app.domain.project

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.goaltree.Assigner

/**
 * 교육 프로젝트의 종류 = 누가 시작했는지. 학생이 스스로 만든 것, 학부모가 자녀에게 준 것, 멘토가 학생에게 준 것.
 * 종류마다 보는 사람이 정해집니다([audience], 판단은 [ProjectViewer.canSee] 한 곳).
 */
enum class ProjectKind(val assigner: Assigner, val chip: String, val label: String, val audience: String) {
    SELF(Assigner.SELF, "스스로 만든", "스스로 만든 프로젝트", "학생만 봐요"),
    PARENT(Assigner.PARENT, "학부모가 준", "학부모가 준 프로젝트", "준 학부모와 학생만 봐요"),
    MENTOR(Assigner.MENTOR, "멘토가 준", "멘토가 준 프로젝트", "준 멘토와 학생만 봐요");

    companion object {
        /** 저장된 프로젝트의 종류(createdByRole). 누가 만들었는지 모르는 옛 기록이면 null. */
        fun of(goal: GoalEntity): ProjectKind? = entries.firstOrNull { it.assigner.role.name == goal.createdByRole }

        /** 이 역할이 지금 시작하면 생길 종류. 역할이 없으면 스스로. */
        fun startedBy(role: Role?): ProjectKind = entries.firstOrNull { it.assigner.role == role } ?: SELF
    }
}
