package com.nextstep.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nextstep.app.data.model.Role
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.calendar.CalendarScreen
import com.nextstep.app.ui.grades.GradesScreen
import com.nextstep.app.ui.home.StudentHomeScreen
import com.nextstep.app.ui.insights.InsightsScreen
import com.nextstep.app.ui.onboarding.OnboardingScreen
import com.nextstep.app.ui.parent.ParentDashboardScreen
import com.nextstep.app.ui.progress.ProgressScreen
import com.nextstep.app.ui.progress.SubjectDetailScreen
import com.nextstep.app.ui.settings.SettingsScreen
import com.nextstep.app.ui.timer.TimerScreen

object Routes {
    const val HOME = "home"
    const val PROGRESS = "progress"
    const val CALENDAR = "calendar"
    const val GRADES = "grades"
    const val INSIGHTS = "insights"
    const val TIMER = "timer"
    const val SETTINGS = "settings"
    const val SUBJECT = "subject/{subjectId}"
    fun subject(id: String) = "subject/$id"
}

data class TopLevelDestination(val route: String, val label: String, val icon: ImageVector)

private fun topLevelDestinations(role: Role): List<TopLevelDestination> = listOf(
    if (role == Role.PARENT) TopLevelDestination(Routes.HOME, "대시보드", Icons.Default.Dashboard)
    else TopLevelDestination(Routes.HOME, "홈", Icons.Default.Home),
    TopLevelDestination(Routes.PROGRESS, "진도", Icons.Default.MenuBook),
    TopLevelDestination(Routes.CALENDAR, "캘린더", Icons.Default.CalendarMonth),
    TopLevelDestination(Routes.GRADES, "성적", Icons.Default.BarChart),
    TopLevelDestination(Routes.INSIGHTS, "분석", Icons.Default.Insights),
)

@Composable
fun NextStepRoot(rootViewModel: RootViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val profile by rootViewModel.profile.collectAsStateWithLifecycle()
    val current = profile
    val role = current?.role
    when {
        current == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        !current.onboarded || role == null -> OnboardingScreen()
        else -> MainScaffold(role = role)
    }
}

@Composable
private fun MainScaffold(role: Role) {
    val navController = rememberNavController()
    val destinations = topLevelDestinations(role)
    val backStack by navController.currentBackStackEntryAsState()
    val currentDestination = backStack?.destination
    val showBottomBar = destinations.any { d -> currentDestination?.hierarchy?.any { it.route == d.route } == true }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    destinations.forEach { d ->
                        val selected = currentDestination?.hierarchy?.any { it.route == d.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(d.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(d.icon, contentDescription = d.label) },
                            label = { Text(d.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NextStepNavHost(navController = navController, role = role, modifier = Modifier.padding(padding))
    }
}

@Composable
private fun NextStepNavHost(navController: NavHostController, role: Role, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Routes.HOME, modifier = modifier) {
        composable(Routes.HOME) {
            if (role == Role.PARENT) {
                ParentDashboardScreen(
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenSubject = { navController.navigate(Routes.subject(it)) },
                    onOpenInsights = { navController.navigate(Routes.INSIGHTS) },
                )
            } else {
                StudentHomeScreen(
                    onOpenTimer = { navController.navigate(Routes.TIMER) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenSubject = { navController.navigate(Routes.subject(it)) },
                )
            }
        }
        composable(Routes.PROGRESS) {
            ProgressScreen(role = role, onOpenSubject = { navController.navigate(Routes.subject(it)) })
        }
        composable(
            Routes.SUBJECT,
            arguments = listOf(navArgument("subjectId") { type = NavType.StringType }),
        ) {
            SubjectDetailScreen(role = role, onBack = { navController.popBackStack() })
        }
        composable(Routes.CALENDAR) { CalendarScreen(role = role) }
        composable(Routes.GRADES) { GradesScreen(role = role) }
        composable(Routes.INSIGHTS) { InsightsScreen(role = role) }
        composable(Routes.TIMER) { TimerScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.SETTINGS) { SettingsScreen(onBack = { navController.popBackStack() }) }
    }
}
