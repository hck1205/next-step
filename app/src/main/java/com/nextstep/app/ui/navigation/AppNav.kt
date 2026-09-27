package com.nextstep.app.ui.navigation

import androidx.navigation.NavHostController
import com.nextstep.app.domain.hub.ConcernSection

/** 화면 사이 이동 콜백. 한 번 만들어 모든 경로가 같이 씁니다. */
internal class AppNav(private val controller: NavHostController) {
    val go: (String) -> Unit = { controller.navigate(it) }
    val back: () -> Unit = { controller.popBackStack() }
    val openSubject: (String) -> Unit = { go(Routes.subject(it)) }
    val openRecords: (ConcernSection) -> Unit = { go(Routes.records(it)) }
    val openProject: (String) -> Unit = { go(Routes.project(it)) }
    val openGoal: (String) -> Unit = { go(Routes.goal(it)) }
    fun to(route: String): () -> Unit = { go(route) }
}
