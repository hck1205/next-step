package com.nextstep.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.nextstep.app.ui.activities.ActivitiesActions
import com.nextstep.app.ui.activities.ActivitiesScreen
import com.nextstep.app.ui.content.ContentActions
import com.nextstep.app.ui.curriculum.CurriculumActions
import com.nextstep.app.ui.curriculum.CurriculumScreen
import com.nextstep.app.ui.content.ContentLibraryScreen
import com.nextstep.app.ui.goals.GoalsActions
import com.nextstep.app.ui.goals.GoalsScreen
import com.nextstep.app.ui.home.HomeActions
import com.nextstep.app.ui.home.StudentHomeScreen
import com.nextstep.app.ui.journey.JourneyActions
import com.nextstep.app.ui.journey.JourneyScreen
import com.nextstep.app.ui.mentor.MentorDashboardActions
import com.nextstep.app.ui.mentor.MentorDashboardScreen
import com.nextstep.app.ui.onboarding.OnboardingScreen
import com.nextstep.app.ui.parent.CheerScreen
import com.nextstep.app.ui.parent.ParentDashboardActions
import com.nextstep.app.ui.parent.ParentDashboardScreen
import com.nextstep.app.ui.progress.SubjectDetailActions
import com.nextstep.app.ui.progress.SubjectDetailScreen
import com.nextstep.app.ui.quickadd.QuickAddSheet
import com.nextstep.app.ui.records.RecordSegment
import com.nextstep.app.ui.records.RecordsActions
import com.nextstep.app.ui.records.RecordsScreen
import com.nextstep.app.ui.roadmap.RoadmapActions
import com.nextstep.app.ui.roadmap.RoadmapScreen
import com.nextstep.app.ui.settings.SettingsActions
import com.nextstep.app.ui.settings.SettingsScreen
import com.nextstep.app.ui.timer.TimerActions
import com.nextstep.app.ui.timer.TimerScreen

object Routes {
    const val HOME = "home"
    const val JOURNEY = "journey"
    const val RECORDS = "records/{segment}"
    const val FAMILY = "family"
    const val GOALS = "goals"
    const val ACTIVITIES = "activities"
    const val ROADMAP = "roadmap"
    const val MENTOR_HOME = "mentor"
    const val CONTENT = "content"
    const val CHEER = "cheer"
    const val CURRICULUM = "curriculum"
    const val TIMER = "timer"
    const val SUBJECT = "subject/{subjectId}"
    fun subject(id: String) = "subject/$id"
    fun records(segment: RecordSegment = RecordSegment.BALANCE) = "records/${segment.route}"
    private const val RECORDS_PREFIX = "records/"
    fun isRecords(route: String?) = route?.startsWith(RECORDS_PREFIX) == true
}

data class TopLevelDestination(val route: String, val label: String, val icon: ImageVector)

/**
 * 모든 역할이 같은 뼈대를 씁니다: 오늘 · 여정 · 기록(학생은 "나") · 가족, 가운데 + 는 기록하기 시트.
 * 화면마다 질문 하나("지금 뭐 하지?", "다음은?", "어떻게 하고 있지?", "누구와?")에만 답하고, 새 기능은 탭이 아니라 카드·항목·세그먼트로 들어갑니다.
 */
private fun topLevelDestinations(role: Role): List<TopLevelDestination> = listOf(
    TopLevelDestination(Routes.HOME, "오늘", Icons.Default.WbSunny),
    TopLevelDestination(Routes.JOURNEY, "여정", Icons.Default.Timeline),
    TopLevelDestination(Routes.RECORDS, if (role == Role.STUDENT) "나" else "기록", Icons.Default.BarChart),
    TopLevelDestination(Routes.FAMILY, "가족", Icons.Default.Group),
)

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
    val onTopLevel = destinations.any { d -> currentDestination?.hierarchy?.any { it.route == d.route } == true }
    var showQuickAdd by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (onTopLevel) {
                NavigationBar {
                    destinations.forEach { d ->
                        val selected = currentDestination?.hierarchy?.any { it.route == d.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(if (d.route == Routes.RECORDS) Routes.records() else d.route) {
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
        floatingActionButton = {
            if (onTopLevel) FloatingActionButton(onClick = { showQuickAdd = true }) { Icon(Icons.Default.Add, contentDescription = "기록하기") }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { padding ->
        NextStepNavHost(navController = navController, caps = caps, modifier = Modifier.padding(padding))
    }

    if (showQuickAdd) {
        QuickAddSheet(caps = caps, onDismiss = { showQuickAdd = false }, onOpenTimer = { navController.navigate(Routes.TIMER) })
    }
}

@Composable
private fun NextStepNavHost(navController: NavHostController, caps: Capabilities, modifier: Modifier = Modifier) {
    val go: (String) -> Unit = { navController.navigate(it) }
    val back: () -> Unit = { navController.popBackStack() }
    val openSubject: (String) -> Unit = { go(Routes.subject(it)) }
    val openRecords: (RecordSegment) -> Unit = { go(Routes.records(it)) }

    NavHost(navController = navController, startDestination = Routes.HOME, modifier = modifier) {
        composable(Routes.HOME) {
            when (caps.role) {
                Role.PARENT -> ParentDashboardScreen(
                    caps = caps,
                    actions = ParentDashboardActions(
                        onOpenSettings = { go(Routes.FAMILY) }, onOpenSubject = openSubject, onOpenRecords = openRecords,
                        onOpenMentor = { go(Routes.MENTOR_HOME) }, onOpenRoadmap = { go(Routes.ROADMAP) }, onOpenContent = { go(Routes.CONTENT) },
                        onOpenJourney = { go(Routes.JOURNEY) }, onOpenCheer = { go(Routes.CHEER) }, onOpenGoals = { go(Routes.GOALS) },
                    ),
                )
                Role.MENTOR -> MentorDashboardScreen(
                    actions = MentorDashboardActions(
                        onOpenSettings = { go(Routes.FAMILY) }, onOpenSubject = openSubject, onOpenRoadmap = { go(Routes.ROADMAP) },
                        onOpenContent = { go(Routes.CONTENT) }, onBack = null, onOpenJourney = { go(Routes.JOURNEY) },
                    ),
                )
                Role.STUDENT -> StudentHomeScreen(
                    actions = HomeActions(
                        onOpenTimer = { go(Routes.TIMER) }, onOpenSettings = { go(Routes.FAMILY) }, onOpenSubject = openSubject,
                        onOpenRoadmap = { go(Routes.ROADMAP) }, onOpenContent = { go(Routes.CONTENT) }, onOpenJourney = { go(Routes.JOURNEY) },
                        onOpenRecords = openRecords, onOpenCurriculum = { go(Routes.CURRICULUM) }, onOpenGoals = { go(Routes.GOALS) },
                    ),
                )
            }
        }
        composable(Routes.JOURNEY) {
            JourneyScreen(caps = caps, actions = JourneyActions(onBack = null, onOpenSettings = { go(Routes.FAMILY) }, onOpenGoals = { go(Routes.GOALS) }, onOpenActivities = { go(Routes.ACTIVITIES) }, onOpenCurriculum = { go(Routes.CURRICULUM) }))
        }
        composable(Routes.RECORDS, arguments = listOf(navArgument("segment") { type = NavType.StringType; defaultValue = RecordSegment.BALANCE.route })) { entry ->
            RecordsScreen(
                caps = caps,
                actions = RecordsActions(onOpenSubject = openSubject, onOpenRoadmap = { go(Routes.ROADMAP) }, onOpenActivities = { go(Routes.ACTIVITIES) }, onOpenJourney = { go(Routes.JOURNEY) }),
                initialSegment = RecordSegment.from(entry.arguments?.getString("segment")),
            )
        }
        composable(Routes.FAMILY) { SettingsScreen(caps = caps, actions = SettingsActions(onBack = null, onOpenContent = { go(Routes.CONTENT) })) }

        composable(Routes.MENTOR_HOME) {
            MentorDashboardScreen(
                actions = MentorDashboardActions(
                    onOpenSettings = { go(Routes.FAMILY) }, onOpenSubject = openSubject, onOpenRoadmap = { go(Routes.ROADMAP) },
                    onOpenContent = { go(Routes.CONTENT) }, onBack = back, onOpenJourney = { go(Routes.JOURNEY) },
                ),
            )
        }
        composable(Routes.GOALS) { GoalsScreen(caps = caps, actions = GoalsActions(onBack = back, onOpenJourney = { go(Routes.JOURNEY) })) }
        composable(Routes.ACTIVITIES) { ActivitiesScreen(caps = caps, actions = ActivitiesActions(onBack = back, onOpenJourney = { go(Routes.JOURNEY) })) }
        composable(Routes.ROADMAP) { RoadmapScreen(caps = caps, actions = RoadmapActions(onBack = back, onOpenContent = { go(Routes.CONTENT) })) }
        composable(Routes.CONTENT) { ContentLibraryScreen(caps = caps, actions = ContentActions(onBack = back)) }
        composable(Routes.CHEER) { CheerScreen() }
        composable(Routes.CURRICULUM) { CurriculumScreen(caps = caps, actions = CurriculumActions(onBack = back, onOpenSubject = openSubject, onOpenContent = { go(Routes.CONTENT) })) }
        composable(Routes.SUBJECT, arguments = listOf(navArgument("subjectId") { type = NavType.StringType })) {
            SubjectDetailScreen(caps = caps, actions = SubjectDetailActions(onBack = back))
        }
        composable(Routes.TIMER) { TimerScreen(actions = TimerActions(onBack = back)) }
    }
}
