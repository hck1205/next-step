package com.nextstep.app.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.growth.StudentScreen
import com.nextstep.app.ui.activities.ActivitiesActions
import com.nextstep.app.ui.activities.ActivitiesScreen
import com.nextstep.app.ui.content.ContentActions
import com.nextstep.app.ui.content.ContentLibraryScreen
import com.nextstep.app.ui.curriculum.CurriculumActions
import com.nextstep.app.ui.curriculum.CurriculumScreen
import com.nextstep.app.ui.goal.GoalActions
import com.nextstep.app.ui.goal.GoalScreen
import com.nextstep.app.ui.goals.GoalsActions
import com.nextstep.app.ui.goals.GoalsScreen
import com.nextstep.app.ui.mentor.MentorDashboardActions
import com.nextstep.app.ui.mentor.MentorDashboardScreen
import com.nextstep.app.ui.progress.SubjectDetailActions
import com.nextstep.app.ui.progress.SubjectDetailScreen
import com.nextstep.app.ui.project.ProjectActions
import com.nextstep.app.ui.project.ProjectScreen
import com.nextstep.app.ui.roadmap.RoadmapActions
import com.nextstep.app.ui.roadmap.RoadmapScreen
import com.nextstep.app.ui.settings.SettingsActions
import com.nextstep.app.ui.settings.SettingsScreen
import com.nextstep.app.ui.timer.TimerActions
import com.nextstep.app.ui.timer.TimerScreen

/** 탭에서 여는 화면. 모두 뒤로 가기로 돌아갑니다. */
internal fun NavGraphBuilder.detailRoutes(caps: Capabilities, studentScreen: StudentScreen?, nav: AppNav, onSwitchChild: (String) -> Unit) {
    composable(Routes.SETTINGS) { SettingsScreen(caps = caps, actions = SettingsActions(onBack = nav.back, onOpenContent = nav.to(Routes.CONTENT))) }
    composable(Routes.MENTOR_HOME) { MentorDashboardScreen(actions = mentorActions(nav, onBack = nav.back, onSwitchChild = onSwitchChild)) }
    composable(Routes.GOALS) { GoalsScreen(caps = caps, actions = GoalsActions(onBack = nav.back, onOpenJourney = nav.to(Routes.JOURNEY))) }
    composable(Routes.ACTIVITIES) { ActivitiesScreen(caps = caps, actions = ActivitiesActions(onBack = nav.back, onOpenJourney = nav.to(Routes.JOURNEY))) }
    composable(Routes.ROADMAP) { RoadmapScreen(caps = caps, actions = RoadmapActions(onBack = nav.back, onOpenContent = nav.to(Routes.CONTENT))) }
    composable(Routes.CONTENT) { ContentLibraryScreen(caps = caps, actions = ContentActions(onBack = nav.back)) }
    composable(Routes.CURRICULUM) { CurriculumScreen(caps = caps, actions = CurriculumActions(onBack = nav.back, onOpenSubject = nav.openSubject, onOpenContent = nav.to(Routes.CONTENT))) }
    composable(Routes.SUBJECT, arguments = listOf(navArgument("subjectId") { type = NavType.StringType })) {
        SubjectDetailScreen(caps = caps, actions = SubjectDetailActions(onBack = nav.back))
    }
    composable(Routes.GOAL, arguments = listOf(navArgument("goalId") { type = NavType.StringType })) {
        GoalScreen(caps = caps, actions = GoalActions(onBack = nav.back, onOpenGoal = nav.openGoal))
    }
    composable(Routes.PROJECT, arguments = listOf(navArgument("goalId") { type = NavType.StringType })) {
        ProjectScreen(caps = caps, actions = ProjectActions(onBack = nav.back))
    }
    composable(Routes.TIMER) {
        // 아이 모드: 숫자 대신 줄어드는 원, 길이는 올해 한 번 공부 길이
        val visualMinutes = if (studentScreen?.level?.kid?.visualTimer == true) studentScreen?.year?.sessionMinutes?.takeIf { it > 0 } ?: DEFAULT_KID_TIMER_MINUTES else null
        TimerScreen(actions = TimerActions(onBack = nav.back), visualMinutes = visualMinutes)
    }
}

/** 멘토 첫 화면의 이동. 멘토 본인은 오늘 탭([onBack] 없음), 학부모는 오늘 화면에서 열어 뒤로 갑니다. */
internal fun mentorActions(nav: AppNav, onBack: (() -> Unit)?, onSwitchChild: (String) -> Unit) = MentorDashboardActions(
    onOpenSubject = nav.openSubject, onOpenRoadmap = nav.to(Routes.ROADMAP),
    onOpenContent = nav.to(Routes.CONTENT), onBack = onBack, onOpenJourney = nav.to(Routes.JOURNEY), onSwitchChild = onSwitchChild,
)

/** 올해 프로필이 없을 때 아이용 타이머 길이(분). */
private const val DEFAULT_KID_TIMER_MINUTES = 15
