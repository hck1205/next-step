package com.nextstep.app.domain.growth

import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/**
 * 학생 화면 한 벌: 단계(카드·말투·탭), 올해 프로필(공부 종류·양), 글씨 배율, 할 일 줄 수, 오늘 화면 카드 순서.
 * 기본은 해마다 달라지는 [YearProfile] 을 따르고, 학부모가 단계를 직접 골랐으면 그 단계의 글씨·줄 수를 씁니다.
 */
data class StudentScreen(
    val level: StudentUiLevel,
    val year: YearProfile?,
    val textScale: Float,
    val taskRows: Int,
    val homeOrder: List<StudentHomeSection>,
) {
    companion object {
        /** 카드 기본 순서. 올해 프로필의 lead 가 타이머 바로 뒤로 당겨집니다. */
        val DEFAULT_ORDER: List<StudentHomeSection> = listOf(
            StudentHomeSection.TIMER, StudentHomeSection.CURRICULUM, StudentHomeSection.MISSION, StudentHomeSection.JOURNEY,
            StudentHomeSection.TASKS, StudentHomeSection.CHEERS, StudentHomeSection.TALK, StudentHomeSection.FEEDBACK, StudentHomeSection.MY_WEEK, StudentHomeSection.STUDY_FLOW, StudentHomeSection.ROUTINE, StudentHomeSection.GAME, StudentHomeSection.YEAR, StudentHomeSection.WEEK, StudentHomeSection.EVENTS, StudentHomeSection.FAMILY, StudentHomeSection.EXAM, StudentHomeSection.MY_SCORES,
            StudentHomeSection.RECOMMENDATION, StudentHomeSection.SUBJECTS, StudentHomeSection.REVIEW,
            StudentHomeSection.PREVIEW, StudentHomeSection.ROADMAP, StudentHomeSection.PLANNER,
        )

        fun homeOrder(year: YearProfile?, level: StudentUiLevel): List<StudentHomeSection> =
            (listOf(StudentHomeSection.TIMER) + year?.lead.orEmpty() + DEFAULT_ORDER).distinct().filter(level::shows)

        /** 학생 정보가 없으면 전체 화면(나무). */
        fun of(student: MemberEntity?, today: LocalDate = DateUtils.today()): StudentScreen {
            val year = student?.let { YearProfiles.of(it, today) }
            val chosen = StudentUiLevel.fromName(student?.uiLevel)
            val level = chosen ?: year?.level ?: StudentUiLevel.TREE
            // 학부모가 단계를 직접 고르지 않았으면 올해 프로필의 글씨·줄 수를 따릅니다.
            val followed = year?.takeIf { chosen == null }
            return StudentScreen(
                level = level,
                year = year,
                textScale = followed?.textScale ?: level.textScale,
                taskRows = followed?.taskRows ?: level.taskRows,
                homeOrder = homeOrder(year, level),
            )
        }
    }
}
