package com.nextstep.app.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.growth.KidMode
import com.nextstep.app.domain.growth.StudentScreen
import com.nextstep.app.domain.hub.ConcernSection
import com.nextstep.app.domain.hub.HubViewer
import com.nextstep.app.ui.home.HomeActions
import com.nextstep.app.ui.home.StudentHomeScreen
import com.nextstep.app.ui.hub.HubActions
import com.nextstep.app.ui.hub.HubScreen
import com.nextstep.app.ui.journey.JourneyActions
import com.nextstep.app.ui.journey.JourneyScreen
import com.nextstep.app.ui.kidfamily.KidFamilyActions
import com.nextstep.app.ui.kidfamily.KidFamilyScreen
import com.nextstep.app.ui.kidme.KidMeScreen
import com.nextstep.app.ui.mentor.MentorDashboardScreen
import com.nextstep.app.ui.parent.ParentDashboardActions
import com.nextstep.app.ui.parent.ParentDashboardScreen
import com.nextstep.app.ui.settings.SettingsActions
import com.nextstep.app.ui.settings.SettingsScreen
import com.nextstep.app.ui.yearplan.YearPlanActions
import com.nextstep.app.ui.yearplan.YearPlanScreen

/** 하단 탭 네 개: 오늘(역할마다 다른 첫 화면) · 올해 또는 여정 · 기록 · 가족. */
internal fun NavGraphBuilder.tabRoutes(caps: Capabilities, studentScreen: StudentScreen?, nav: AppNav, onSwitchChild: (String) -> Unit) {
    val studentLevel = studentScreen?.level
    val kid = studentLevel?.kid ?: KidMode.NONE
    todayRoute(caps, nav, onSwitchChild)
    composable(Routes.YEAR) {
        // 학생은 하단 탭(여정은 위 버튼), 학부모·멘토는 여정에서 열어 뒤로 가기로 돌아갑니다.
        YearPlanScreen(
            caps = caps,
            actions = if (caps.isStudent) YearPlanActions(onOpenJourney = if (studentLevel?.showsJourneyTab != false) nav.to(Routes.JOURNEY) else null)
            else YearPlanActions(onBack = nav.back),
        )
    }
    composable(Routes.JOURNEY) {
        JourneyScreen(
            caps = caps,
            actions = JourneyActions(
                onBack = null, onOpenGoals = nav.to(Routes.GOALS), onOpenActivities = nav.to(Routes.ACTIVITIES),
                onOpenCurriculum = nav.to(Routes.CURRICULUM), onOpenYear = if (caps.isStudent) null else nav.to(Routes.YEAR),
            ),
        )
    }
    composable(Routes.RECORDS, arguments = listOf(navArgument("section") { type = NavType.StringType; defaultValue = ConcernSection.OVERVIEW.route })) { entry ->
        // 아이 모드(학령 전·초1~2): 기록 허브 대신 스티커판
        if (kid.stickerMe) KidMeScreen() else HubScreen(
            caps = caps, studentLevel = studentLevel,
            actions = HubActions(onOpenSubject = nav.openSubject, onOpenJourney = nav.to(Routes.JOURNEY), onOpenProject = nav.openProject, onOpenGoal = nav.openGoal),
            initialSection = ConcernSection.from(entry.arguments?.getString("section"), HubViewer.of(caps, studentLevel)),
        )
    }
    composable(Routes.FAMILY) {
        // 아이 모드: 가족 얼굴과 한 번 누르는 말. 설정은 어른 확인 뒤 별도 화면으로.
        if (kid.kidFamily) KidFamilyScreen(actions = KidFamilyActions(onOpenSettings = nav.to(Routes.SETTINGS)))
        else SettingsScreen(caps = caps, actions = SettingsActions(onBack = null, onOpenContent = nav.to(Routes.CONTENT)))
    }
}

/** 오늘 탭은 역할마다 다른 첫 화면이라 여기서만 역할로 고릅니다(화면 안의 분기는 caps). */
private fun NavGraphBuilder.todayRoute(caps: Capabilities, nav: AppNav, onSwitchChild: (String) -> Unit) {
    composable(Routes.HOME) {
        when (caps.role) {
            Role.PARENT -> ParentDashboardScreen(
                caps = caps,
                actions = ParentDashboardActions(
                    onOpenSettings = nav.to(Routes.FAMILY), onOpenRecords = nav.openRecords,
                    onOpenMentor = nav.to(Routes.MENTOR_HOME), onOpenContent = nav.to(Routes.CONTENT),
                    onOpenJourney = nav.to(Routes.JOURNEY), onOpenGoals = nav.to(Routes.GOALS),
                    onSwitchChild = onSwitchChild, onOpenProject = nav.openProject, onOpenGoal = nav.openGoal,
                ),
            )
            Role.MENTOR -> MentorDashboardScreen(actions = mentorActions(nav, onBack = null, onSwitchChild = onSwitchChild))
            Role.STUDENT -> StudentHomeScreen(
                actions = HomeActions(
                    onOpenTimer = nav.to(Routes.TIMER), onOpenSubject = nav.openSubject,
                    onOpenRoadmap = nav.to(Routes.ROADMAP), onOpenContent = nav.to(Routes.CONTENT), onOpenJourney = nav.to(Routes.JOURNEY),
                    onOpenRecords = nav.openRecords, onOpenCurriculum = nav.to(Routes.CURRICULUM), onOpenGoals = nav.to(Routes.GOALS),
                    onOpenYear = nav.to(Routes.YEAR), onOpenProject = nav.openProject,
                ),
            )
        }
    }
}
