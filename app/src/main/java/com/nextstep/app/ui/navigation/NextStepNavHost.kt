package com.nextstep.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Timeline
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
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.calendar.CalendarScreen
import com.nextstep.app.ui.content.ContentLibraryScreen
import com.nextstep.app.ui.grades.GradesScreen
import com.nextstep.app.ui.home.StudentHomeScreen
import com.nextstep.app.ui.insights.InsightsScreen
import com.nextstep.app.ui.goals.GoalsActions
import com.nextstep.app.ui.goals.GoalsScreen
import com.nextstep.app.ui.journey.JourneyActions
import com.nextstep.app.ui.journey.JourneyScreen
import com.nextstep.app.ui.mentor.MentorDashboardScreen
import com.nextstep.app.ui.onboarding.OnboardingScreen
import com.nextstep.app.ui.parent.CheerScreen
import com.nextstep.app.ui.parent.ParentDashboardScreen
import com.nextstep.app.ui.progress.ProgressScreen
import com.nextstep.app.ui.progress.SubjectDetailScreen
import com.nextstep.app.ui.roadmap.RoadmapScreen
import com.nextstep.app.ui.settings.SettingsScreen
import com.nextstep.app.ui.timer.TimerScreen
import com.nextstep.app.ui.content.ContentActions
import com.nextstep.app.ui.home.HomeActions
import com.nextstep.app.ui.insights.InsightsActions
import com.nextstep.app.ui.mentor.MentorDashboardActions
import com.nextstep.app.ui.parent.ParentDashboardActions
import com.nextstep.app.ui.progress.ProgressActions
import com.nextstep.app.ui.progress.SubjectDetailActions
import com.nextstep.app.ui.roadmap.RoadmapActions
import com.nextstep.app.ui.settings.SettingsActions
import com.nextstep.app.ui.timer.TimerActions
import androidx.compose.runtime.setValue

object Routes {
    const val HOME = "home"
    const val PROGRESS = "progress"
    const val CALENDAR = "calendar"
    const val GRADES = "grades"
    const val INSIGHTS = "insights"
    const val CHEER = "cheer"
    const val ROADMAP = "roadmap"
    const val MENTOR_HOME = "mentor"
    const val CONTENT = "content"
    const val TIMER = "timer"
    const val SETTINGS = "settings"
    const val JOURNEY = "journey"
    const val GOALS = "goals"
    const val SUBJECT = "subject/{subjectId}"
    fun subject(id: String) = "subject/$id"
}

data class TopLevelDestination(val route: String, val label: String, val icon: ImageVector)

/**
 * 역할별 하단 탭. 각 역할의 핵심 흐름만 탭으로 두고, 나머지는 화면 안의 진입점으로 연결합니다.
 * - 학생: 학습 내용·커리큘럼·스케줄링 중심
 * - 학부모: 여정(나이대별 준비)·모니터링·격려·분석(재능 발견) 중심. 성적은 대시보드에서 진입
 * - 멘토: 로드맵 큐레이팅·진도 지도 중심
 */
private fun topLevelDestinations(role: Role): List<TopLevelDestination> = when (role) {
    Role.STUDENT -> listOf(
        TopLevelDestination(Routes.HOME, "홈", Icons.Default.Home),
        TopLevelDestination(Routes.PROGRESS, "커리큘럼", Icons.AutoMirrored.Filled.MenuBook),
        TopLevelDestination(Routes.CALENDAR, "캘린더", Icons.Default.CalendarMonth),
        TopLevelDestination(Routes.GRADES, "성적", Icons.Default.BarChart),
        TopLevelDestination(Routes.INSIGHTS, "분석", Icons.Default.Insights),
    )
    Role.PARENT -> listOf(
        TopLevelDestination(Routes.HOME, "대시보드", Icons.Default.Dashboard),
        TopLevelDestination(Routes.JOURNEY, "여정", Icons.Default.Timeline),
        TopLevelDestination(Routes.CHEER, "격려", Icons.Default.Favorite),
        TopLevelDestination(Routes.INSIGHTS, "분석", Icons.Default.Insights),
        TopLevelDestination(Routes.CALENDAR, "캘린더", Icons.Default.CalendarMonth),
    )
    Role.MENTOR -> listOf(
        TopLevelDestination(Routes.HOME, "지도", Icons.Default.Dashboard),
        TopLevelDestination(Routes.ROADMAP, "로드맵", Icons.Default.Map),
        TopLevelDestination(Routes.PROGRESS, "진도", Icons.AutoMirrored.Filled.MenuBook),
        TopLevelDestination(Routes.CALENDAR, "캘린더", Icons.Default.CalendarMonth),
        TopLevelDestination(Routes.GRADES, "성적", Icons.Default.BarChart),
    )
}

@Composable
fun NextStepRoot(rootViewModel: RootViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by rootViewModel.state.collectAsStateWithLifecycle()
    val current = state
    val caps = current?.capabilities
    when {
        current == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        !current.profile.onboarded || caps == null -> OnboardingScreen()
        else -> MainScaffold(caps = caps)
    }
}

@Composable
private fun MainScaffold(caps: Capabilities) {
    val navController = rememberNavController()
    val destinations = topLevelDestinations(caps.role)
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
        NextStepNavHost(navController = navController, caps = caps, modifier = Modifier.padding(padding))
    }
}

@Composable
private fun NextStepNavHost(navController: NavHostController, caps: Capabilities, modifier: Modifier = Modifier) {
    val isTab: (String) -> Boolean = { route -> topLevelDestinations(caps.role).any { it.route == route } }
    val go: (String) -> Unit = { navController.navigate(it) }
    val back: () -> Unit = { navController.popBackStack() }
    val backUnlessTab: (String) -> (() -> Unit)? = { route -> if (isTab(route)) null else back }
    val openSubject: (String) -> Unit = { go(Routes.subject(it)) }

    NavHost(navController = navController, startDestination = Routes.HOME, modifier = modifier) {
        composable(Routes.HOME) {
            when (caps.role) {
                Role.PARENT -> ParentDashboardScreen(
                    caps = caps,
                    actions = ParentDashboardActions(
                        onOpenSettings = { go(Routes.SETTINGS) }, onOpenSubject = openSubject, onOpenInsights = { go(Routes.INSIGHTS) },
                        onOpenMentor = { go(Routes.MENTOR_HOME) }, onOpenRoadmap = { go(Routes.ROADMAP) }, onOpenContent = { go(Routes.CONTENT) },
                        onOpenJourney = { go(Routes.JOURNEY) }, onOpenGrades = { go(Routes.GRADES) },
                    ),
                )
                Role.MENTOR -> MentorDashboardScreen(
                    actions = MentorDashboardActions(
                        onOpenSettings = { go(Routes.SETTINGS) }, onOpenSubject = openSubject, onOpenRoadmap = { go(Routes.ROADMAP) },
                        onOpenContent = { go(Routes.CONTENT) }, onBack = null, onOpenJourney = { go(Routes.JOURNEY) },
                    ),
                )
                Role.STUDENT -> StudentHomeScreen(
                    actions = HomeActions(
                        onOpenTimer = { go(Routes.TIMER) }, onOpenSettings = { go(Routes.SETTINGS) }, onOpenSubject = openSubject,
                        onOpenRoadmap = { go(Routes.ROADMAP) }, onOpenContent = { go(Routes.CONTENT) }, onOpenJourney = { go(Routes.JOURNEY) },
                    ),
                )
            }
        }
        composable(Routes.MENTOR_HOME) {
            MentorDashboardScreen(
                actions = MentorDashboardActions(
                    onOpenSettings = { go(Routes.SETTINGS) }, onOpenSubject = openSubject, onOpenRoadmap = { go(Routes.ROADMAP) },
                    onOpenContent = { go(Routes.CONTENT) }, onBack = back, onOpenJourney = { go(Routes.JOURNEY) },
                ),
            )
        }
        composable(Routes.CONTENT) { ContentLibraryScreen(caps = caps, actions = ContentActions(onBack = back)) }
        composable(Routes.PROGRESS) {
            ProgressScreen(caps = caps, actions = ProgressActions(onOpenSubject = openSubject, onOpenRoadmap = { go(Routes.ROADMAP) }))
        }
        composable(Routes.SUBJECT, arguments = listOf(navArgument("subjectId") { type = NavType.StringType })) {
            SubjectDetailScreen(caps = caps, actions = SubjectDetailActions(onBack = back))
        }
        composable(Routes.ROADMAP) {
            RoadmapScreen(caps = caps, actions = RoadmapActions(onBack = backUnlessTab(Routes.ROADMAP), onOpenContent = { go(Routes.CONTENT) }))
        }
        composable(Routes.CHEER) { CheerScreen() }
        composable(Routes.JOURNEY) { JourneyScreen(caps = caps, actions = JourneyActions(onBack = backUnlessTab(Routes.JOURNEY), onOpenSettings = { go(Routes.SETTINGS) }, onOpenGoals = { go(Routes.GOALS) })) }
        composable(Routes.GOALS) { GoalsScreen(caps = caps, actions = GoalsActions(onBack = back, onOpenJourney = { go(Routes.JOURNEY) })) }
        composable(Routes.CALENDAR) { CalendarScreen(caps = caps) }
        composable(Routes.GRADES) { GradesScreen(caps = caps) }
        composable(Routes.INSIGHTS) { InsightsScreen(caps = caps, actions = InsightsActions(onBack = backUnlessTab(Routes.INSIGHTS))) }
        composable(Routes.TIMER) { TimerScreen(actions = TimerActions(onBack = back)) }
        composable(Routes.SETTINGS) { SettingsScreen(actions = SettingsActions(onBack = back)) }
    }
}
