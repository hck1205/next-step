package com.nextstep.app.domain.hub

import com.nextstep.app.domain.growth.StudentUiLevel

/**
 * 관심사 안의 섹션 하나 = 기능 화면 하나. 한 관심사 안에서는 이 순서가 곧 화면 순서입니다.
 * [minLevel] 은 학생 화면 단계가 이 이상일 때만 보인다는 뜻이고(학부모·멘토는 단계로 줄지 않음),
 * [audiences] 는 이 섹션을 보는 자리입니다(예: 신체 기록은 멘토에게 보이지 않음).
 * [route] 는 내비게이션 인자이며, 오늘 카드의 "전체" 버튼이 이 값으로 해당 섹션을 바로 엽니다.
 */
enum class ConcernSection(
    val concern: Concern,
    val label: String,
    val route: String,
    val minLevel: StudentUiLevel,
    val audiences: Set<HubAudience> = ALL,
) {
    OVERVIEW(Concern.OVERVIEW, "한눈에", "overview", StudentUiLevel.SEED),
    SELF(Concern.STUDY, "스스로", "self", StudentUiLevel.SEEDLING),
    PROGRESS(Concern.STUDY, "진도", "progress", StudentUiLevel.SEEDLING),
    TIME(Concern.STUDY, "시간", "time", StudentUiLevel.SPROUT),
    HABITS(Concern.STUDY, "습관", "habits", StudentUiLevel.SEEDLING),
    CALENDAR(Concern.STUDY, "일정", "calendar", StudentUiLevel.SEEDLING),
    CURRICULUM(Concern.LEARN, "이번 학기", "curriculum", StudentUiLevel.STEM),
    REVIEW(Concern.LEARN, "복습", "review", StudentUiLevel.SEEDLING),
    CONTENT(Concern.LEARN, "영상", "content", StudentUiLevel.SEEDLING),
    ROADMAP(Concern.LEARN, "로드맵", "roadmap", StudentUiLevel.BRANCH),
    PROJECTS(Concern.PROJECT, "진행 중", "projects", StudentUiLevel.SEED),
    PROJECT_CATALOG(Concern.PROJECT, "새로 시작", "project-catalog", StudentUiLevel.STEM),
    MISSIONS(Concern.EXAMS, "시험·목표", "missions", StudentUiLevel.STEM),
    GRADES(Concern.EXAMS, "성적", "grades", StudentUiLevel.STEM),
    GOAL_TREE(Concern.PLAN, "목표", "goal-tree", StudentUiLevel.SEEDLING),
    TODO(Concern.PLAN, "할 일", "todo", StudentUiLevel.SEEDLING),
    ASSIGNMENTS(Concern.PLAN, "과제", "assignments", StudentUiLevel.STEM),
    PLAN_HISTORY(Concern.PLAN, "기록", "plan-history", StudentUiLevel.SEEDLING),
    REWARDS(Concern.PLAN, "보상·배지", "rewards", StudentUiLevel.SEED),
    BODY(Concern.GROWTH, "신체", "body", StudentUiLevel.SEED, audiences = setOf(HubAudience.PARENT, HubAudience.STUDENT)),
    ACTIVITIES(Concern.DISCOVER, "활동", "activities", StudentUiLevel.SEED),
    TALENT(Concern.DISCOVER, "재능", "talent", StudentUiLevel.SEEDLING);

    fun visibleFor(viewer: HubViewer): Boolean = viewer.audience in audiences && (viewer.level == null || viewer.level >= minLevel)

    companion object {
        /** 보이는 섹션(관심사 순서 → 관심사 안 순서). */
        fun visibleFor(viewer: HubViewer): List<ConcernSection> = concernsFor(viewer).flatMap { sectionsOf(it, viewer) }

        /** 보이는 섹션이 하나라도 있는 관심사(보는 사람의 순서). */
        fun concernsFor(viewer: HubViewer): List<Concern> = viewer.audience.order.filter { c -> entries.any { it.concern == c && it.visibleFor(viewer) } }

        fun sectionsOf(concern: Concern, viewer: HubViewer): List<ConcernSection> = entries.filter { it.concern == concern && it.visibleFor(viewer) }

        /** 내비게이션 인자에서 섹션을 고릅니다. 모르는 값이거나 안 보이면 같은 관심사의 첫 섹션, 그것도 없으면 한눈에. */
        fun from(route: String?, viewer: HubViewer): ConcernSection {
            val asked = entries.firstOrNull { it.route == route } ?: return OVERVIEW
            if (asked.visibleFor(viewer)) return asked
            return sectionsOf(asked.concern, viewer).firstOrNull() ?: OVERVIEW
        }
    }
}

private val ALL: Set<HubAudience> = HubAudience.entries.toSet()
