package com.nextstep.app.domain.project

/**
 * 기본 교육 프로젝트. 한 프로젝트는 "언제까지 무엇을 할 수 있게"를 단계로 나누고, 단계마다
 * 하루 루틴(무엇을 · 몇 분 · 주 며칠) · 통과 기준 · 추천 교재를 적습니다. 루틴의 양은 단계마다 조금씩 늘어나고
 * 학령 전에는 성장 단계별 권장 시간(YearProfiles.dailyMinutes)을 넘지 않게 잡았습니다.
 * 개월 수는 만 나이 기준이며, 초등 학년의 시작은 초1 = 78개월로 어림합니다(3월 입학).
 * id·key 는 저장된 프로젝트와 연결되므로 바꾸지 않습니다.
 */
object ProjectCatalog {
    /** 공부와 이어지는 것 먼저, 그다음 공부 밖의 것. 순서가 목록 순서입니다. */
    val plans: List<ProjectPlan> = LearningProjects.all + ActivityProjects.all

    val byId: Map<String, ProjectPlan> = plans.associateBy { it.id }

    fun byCategory(category: ProjectCategory): List<ProjectPlan> = plans.filter { it.category == category }
}
