package com.nextstep.app.domain.hub

import com.nextstep.app.domain.growth.StudentUiLevel

/**
 * 관심사 안의 섹션 하나 = 기능 화면 하나. 순서가 곧 화면 순서입니다.
 * [minLevel] 은 학생 화면 단계가 이 이상일 때만 보인다는 뜻입니다(학부모·멘토는 전부 봅니다).
 * [route] 는 내비게이션 인자이며, 오늘 카드의 "전체" 버튼이 이 값으로 해당 섹션을 바로 엽니다.
 */
enum class ConcernSection(val concern: Concern, val label: String, val route: String, val minLevel: StudentUiLevel) {
    OVERVIEW(Concern.OVERVIEW, "한눈에", "overview", StudentUiLevel.SPROUT),
    PROGRESS(Concern.STUDY, "진도", "progress", StudentUiLevel.SEEDLING),
    TIME(Concern.STUDY, "시간", "time", StudentUiLevel.SPROUT),
    CALENDAR(Concern.STUDY, "일정", "calendar", StudentUiLevel.SEEDLING),
    CURRICULUM(Concern.STUDY, "이번 학기", "curriculum", StudentUiLevel.STEM),
    CONTENT(Concern.STUDY, "영상", "content", StudentUiLevel.SEEDLING),
    ROADMAP(Concern.STUDY, "로드맵", "roadmap", StudentUiLevel.BRANCH),
    MISSIONS(Concern.EXAMS, "시험·목표", "missions", StudentUiLevel.STEM),
    GRADES(Concern.EXAMS, "성적", "grades", StudentUiLevel.STEM),
    BODY(Concern.GROWTH, "신체", "body", StudentUiLevel.SPROUT),
    ACTIVITIES(Concern.DISCOVER, "활동", "activities", StudentUiLevel.SPROUT),
    TALENT(Concern.DISCOVER, "재능", "talent", StudentUiLevel.SEEDLING);

    fun visibleFor(level: StudentUiLevel?): Boolean = level == null || level >= minLevel

    companion object {
        /** 보이는 섹션(화면 순서). [level] 이 null 이면 학부모·멘토: 전부. */
        fun visibleFor(level: StudentUiLevel?): List<ConcernSection> = entries.filter { it.visibleFor(level) }

        /** 보이는 섹션이 하나라도 있는 관심사(화면 순서). */
        fun concernsFor(level: StudentUiLevel?): List<Concern> = visibleFor(level).map { it.concern }.distinct()

        fun sectionsOf(concern: Concern, level: StudentUiLevel?): List<ConcernSection> = visibleFor(level).filter { it.concern == concern }

        /** 내비게이션 인자에서 섹션을 고릅니다. 모르는 값이거나 이 단계에서 안 보이면 같은 관심사의 첫 섹션, 그것도 없으면 한눈에. */
        fun from(route: String?, level: StudentUiLevel?): ConcernSection {
            val asked = entries.firstOrNull { it.route == route } ?: return OVERVIEW
            if (asked.visibleFor(level)) return asked
            return sectionsOf(asked.concern, level).firstOrNull() ?: OVERVIEW
        }
    }
}
