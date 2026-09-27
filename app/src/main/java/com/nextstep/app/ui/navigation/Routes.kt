package com.nextstep.app.ui.navigation

import com.nextstep.app.domain.hub.ConcernSection

/** 앱의 모든 경로. 인자가 있는 경로는 만드는 함수와 짝입니다. */
object Routes {
    const val HOME = "home"
    const val JOURNEY = "journey"
    const val RECORDS = "records/{section}"
    const val FAMILY = "family"
    const val GOALS = "goals"
    const val ACTIVITIES = "activities"
    const val ROADMAP = "roadmap"
    const val MENTOR_HOME = "mentor"
    const val CONTENT = "content"
    const val CURRICULUM = "curriculum"
    const val TIMER = "timer"
    const val YEAR = "year"
    /** 아이 모드에서 어른 확인 뒤 여는 설정(보통 모드에서는 가족 탭이 곧 설정). */
    const val SETTINGS = "settings"
    const val SUBJECT = "subject/{subjectId}"
    fun subject(id: String) = "subject/$id"
    const val GOAL = "goal/{goalId}"
    fun goal(goalId: String) = "goal/$goalId"
    const val PROJECT = "project/{goalId}"
    fun project(goalId: String) = "project/$goalId"
    fun records(section: ConcernSection = ConcernSection.OVERVIEW) = "records/${section.route}"
    private const val RECORDS_PREFIX = "records/"
    fun isRecords(route: String?) = route?.startsWith(RECORDS_PREFIX) == true
}
