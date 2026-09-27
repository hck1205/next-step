package com.nextstep.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.growth.StudentScreen

/** 경로 목록은 목차만: 탭 화면은 [tabRoutes], 탭에서 여는 화면은 [detailRoutes]. */
@Composable
internal fun NextStepNavHost(navController: NavHostController, caps: Capabilities, studentScreen: StudentScreen?, onSwitchChild: (String) -> Unit, modifier: Modifier = Modifier) {
    val nav = remember(navController) { AppNav(navController) }
    NavHost(navController = navController, startDestination = Routes.HOME, modifier = modifier) {
        tabRoutes(caps, studentScreen, nav, onSwitchChild)
        detailRoutes(caps, studentScreen, nav, onSwitchChild)
    }
}
