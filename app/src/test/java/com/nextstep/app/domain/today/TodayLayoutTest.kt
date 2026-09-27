package com.nextstep.app.domain.today

import com.nextstep.app.domain.growth.KidMode
import com.nextstep.app.domain.growth.StudentHomeSection
import com.nextstep.app.domain.growth.StudentScreen
import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.domain.hub.Concern
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TodayLayoutTest {
    @Test
    fun cardsGroupByConcernInFirstSeenOrderKeepingCardOrder() {
        val order = listOf(StudentHomeSection.MISSION, StudentHomeSection.TASKS, StudentHomeSection.EXAM, StudentHomeSection.MY_WEEK, StudentHomeSection.GAME, StudentHomeSection.WEEK)
        val groups = TodayLayout.group(order) { it.concern }
        assertEquals(listOf(Concern.EXAMS, Concern.PLAN, Concern.STUDY), groups.map { it.concern })
        assertEquals(listOf(StudentHomeSection.MISSION, StudentHomeSection.EXAM), groups[0].cards)
        assertEquals(listOf(StudentHomeSection.TASKS, StudentHomeSection.GAME), groups[1].cards)
        assertEquals(order.toSet(), TodayLayout.cardsOf(groups, null).toSet())
        assertEquals(listOf(StudentHomeSection.MY_WEEK, StudentHomeSection.WEEK), TodayLayout.cardsOf(groups, Concern.STUDY))
        assertTrue(TodayLayout.cardsOf(groups, Concern.GROWTH).isEmpty())
    }

    @Test
    fun shortcutCardsGoToTheEndOfTheirGroupOnly() {
        val order = listOf(StudentHomeSection.PLANNER, StudentHomeSection.TASKS, StudentHomeSection.GAME, StudentHomeSection.MY_WEEK)
        val groups = TodayLayout.group(order, isShortcut = { it.shortcut }) { it.concern }
        assertEquals(listOf(Concern.PLAN, Concern.STUDY), groups.map { it.concern }) // 묶음 순서는 그대로
        assertEquals(listOf(StudentHomeSection.TASKS, StudentHomeSection.GAME, StudentHomeSection.PLANNER), groups[0].cards)
        assertEquals(listOf(StudentHomeSection.PLANNER), StudentHomeSection.entries.filter { it.shortcut })
    }

    @Test
    fun todayCardsUseTheSameConcernsAsTheRecordsHub() {
        // 기록 탭과 같은 분류: 할 일·레벨은 목표·할 일, 이번 주·일정·진도는 공부, 루틴은 교육 프로젝트, 시험은 시험·성적
        assertEquals(Concern.PLAN, StudentHomeSection.TASKS.concern); assertEquals(Concern.PLAN, StudentHomeSection.GAME.concern)
        assertEquals(Concern.STUDY, StudentHomeSection.MY_WEEK.concern); assertEquals(Concern.PROJECT, StudentHomeSection.ROUTINE.concern)
        assertEquals(Concern.EXAMS, StudentHomeSection.MISSION.concern); assertEquals(Concern.LEARN, StudentHomeSection.REVIEW.concern)
        assertEquals(Concern.PROJECT, ParentTodayCard.ROUTINE.concern); assertEquals(Concern.STUDY, ParentTodayCard.WEEK.concern)
        assertEquals(Concern.PLAN, MentorTodayCard.TASKS.concern); assertEquals(Concern.EXAMS, MentorTodayCard.GRADES.concern)
        // 학부모·멘토 카드 순서대로 묶으면 한눈에 → 목표·할 일 → 공부 순
        assertEquals(listOf(Concern.OVERVIEW, Concern.PLAN, Concern.STUDY, Concern.PROJECT, Concern.EXAMS), TodayLayout.group(ParentTodayCard.entries) { it.concern }.map { it.concern })
        assertEquals(listOf(Concern.OVERVIEW, Concern.PLAN, Concern.STUDY, Concern.LEARN, Concern.EXAMS), TodayLayout.group(MentorTodayCard.entries) { it.concern }.map { it.concern })
    }

    @Test
    fun youngChildrenKeepOneColumnAndOlderOnesGetConcernSlides() {
        assertTrue(StudentUiLevel.SEED.kid.oneColumnToday); assertTrue(StudentUiLevel.SPROUT.kid.oneColumnToday)
        assertFalse(StudentUiLevel.SEEDLING.kid.oneColumnToday); assertFalse(KidMode.NONE.oneColumnToday)
        // 올해 프로필이 앞에 둔 카드(중2: 시험·목표)의 관심사가 첫 묶음
        val m2 = StudentScreen.homeOrder(com.nextstep.app.domain.growth.YearProfiles.byKey.getValue("m2"), StudentUiLevel.BRANCH).filter { it != StudentHomeSection.TIMER }
        assertEquals(Concern.EXAMS, TodayLayout.group(m2) { it.concern }.first().concern)
    }
}
