package com.nextstep.app.domain.goaltree

import com.nextstep.app.data.model.Role

/**
 * 할 일·목표를 누가 냈는지(만든 사람의 역할). 화면의 "스스로 정한 일 · 학부모가 준 일", 기록의 "누가 준 할 일" 묶음이 모두 이 말을 씁니다.
 */
enum class Assigner(val role: Role, val label: String, val taskLabel: String, val goalLabel: String) {
    SELF(Role.STUDENT, "스스로", "스스로 정한 일", "스스로 만든 목표"),
    PARENT(Role.PARENT, "학부모가", "학부모가 준 일", "학부모가 만든 목표"),
    MENTOR(Role.MENTOR, "멘토가", "멘토가 준 일", "멘토가 만든 목표");

    companion object {
        /** 역할 이름(createdByRole)으로. 모르는 값이면 null. */
        fun of(roleName: String?): Assigner? = entries.firstOrNull { it.role.name == roleName }
    }
}
