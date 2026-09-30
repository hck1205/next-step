package com.nextstep.app.ui.hub

import androidx.compose.runtime.Composable
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.domain.hub.ConcernSection
import com.nextstep.app.domain.hub.HubViewer
import com.nextstep.app.ui.activities.ActivitiesActions
import com.nextstep.app.ui.activities.ActivitiesScreen
import com.nextstep.app.ui.assignments.AssignmentsScreen
import com.nextstep.app.ui.calendar.CalendarScreen
import com.nextstep.app.ui.content.ContentActions
import com.nextstep.app.ui.content.ContentLibraryScreen
import com.nextstep.app.ui.curriculum.CurriculumActions
import com.nextstep.app.ui.curriculum.CurriculumScreen
import com.nextstep.app.ui.familycalendar.FamilyCalendarScreen
import com.nextstep.app.ui.familytalk.FamilyTalkScreen
import com.nextstep.app.ui.goals.GoalsActions
import com.nextstep.app.ui.goals.GoalsScreen
import com.nextstep.app.ui.goaltree.GoalTreeActions
import com.nextstep.app.ui.goaltree.GoalTreeScreen
import com.nextstep.app.ui.grades.GradesScreen
import com.nextstep.app.ui.growth.GrowthScreen
import com.nextstep.app.ui.growthalbum.GrowthAlbumScreen
import com.nextstep.app.ui.habits.HabitsScreen
import com.nextstep.app.ui.insights.InsightsScreen
import com.nextstep.app.ui.lessons.LessonsScreen
import com.nextstep.app.ui.overview.OverviewActions
import com.nextstep.app.ui.overview.OverviewScreen
import com.nextstep.app.ui.periodreport.PeriodReportScreen
import com.nextstep.app.ui.planhistory.PlanHistoryActions
import com.nextstep.app.ui.planhistory.PlanHistoryScreen
import com.nextstep.app.ui.progress.ProgressActions
import com.nextstep.app.ui.progress.ProgressScreen
import com.nextstep.app.ui.projectcatalog.ProjectCatalogActions
import com.nextstep.app.ui.projectcatalog.ProjectCatalogScreen
import com.nextstep.app.ui.projects.ProjectsActions
import com.nextstep.app.ui.projects.ProjectsScreen
import com.nextstep.app.ui.review.ReviewScreen
import com.nextstep.app.ui.rewards.RewardsActions
import com.nextstep.app.ui.rewards.RewardsScreen
import com.nextstep.app.ui.roadmap.RoadmapActions
import com.nextstep.app.ui.roadmap.RoadmapScreen
import com.nextstep.app.ui.selfdirection.SelfDirectionScreen
import com.nextstep.app.ui.talent.TalentScreen
import com.nextstep.app.ui.todo.TodoActions
import com.nextstep.app.ui.todo.TodoScreen

/** 섹션 → 기능 화면. 기능 화면은 onBack 없이 그려져 제목줄 대신 관심사·섹션 줄을 씁니다. */
@Composable
internal fun HubSectionContent(section: ConcernSection, caps: Capabilities, viewer: HubViewer, concerns: List<Concern>, actions: HubActions, open: (ConcernSection) -> Unit, openConcern: (Concern) -> Unit) {
    when (section) {
        ConcernSection.OVERVIEW -> OverviewScreen(concerns = concerns, showsBalance = caps.canSeeBalance, actions = OverviewActions(onOpenConcern = openConcern))
        ConcernSection.SELF -> SelfDirectionScreen(caps = caps)
        ConcernSection.PROGRESS -> ProgressScreen(caps = caps, actions = ProgressActions(onOpenSubject = actions.onOpenSubject))
        ConcernSection.TIME -> InsightsScreen(caps = caps)
        ConcernSection.HABITS -> HabitsScreen()
        ConcernSection.CALENDAR -> CalendarScreen(caps = caps)
        ConcernSection.CURRICULUM -> CurriculumScreen(caps = caps, actions = CurriculumActions(onOpenSubject = actions.onOpenSubject, onOpenContent = { open(ConcernSection.CONTENT) }))
        ConcernSection.REVIEW -> ReviewScreen(caps = caps)
        ConcernSection.CONTENT -> ContentLibraryScreen(caps = caps, actions = ContentActions())
        ConcernSection.ROADMAP -> RoadmapScreen(caps = caps, actions = RoadmapActions(onOpenContent = { open(ConcernSection.CONTENT) }))
        ConcernSection.PROJECTS -> ProjectsScreen(
            actions = ProjectsActions(onOpenProject = actions.onOpenProject, onBrowse = if (ConcernSection.PROJECT_CATALOG.visibleFor(viewer)) ({ open(ConcernSection.PROJECT_CATALOG) }) else null),
        )
        ConcernSection.PROJECT_CATALOG -> ProjectCatalogScreen(caps = caps, actions = ProjectCatalogActions(onStarted = { open(ConcernSection.PROJECTS) }))
        ConcernSection.MISSIONS -> GoalsScreen(caps = caps, actions = GoalsActions(onOpenJourney = actions.onOpenJourney))
        ConcernSection.GRADES -> GradesScreen(caps = caps)
        ConcernSection.GOAL_TREE -> GoalTreeScreen(caps = caps, actions = GoalTreeActions(onOpenGoal = actions.onOpenGoal))
        ConcernSection.TODO -> TodoScreen(caps = caps, actions = TodoActions(onOpenGoal = actions.onOpenGoal, onOpenSubject = actions.onOpenSubject))
        ConcernSection.ASSIGNMENTS -> AssignmentsScreen()
        ConcernSection.PLAN_HISTORY -> PlanHistoryScreen(actions = PlanHistoryActions(onOpenGoal = actions.onOpenGoal))
        ConcernSection.LESSONS -> LessonsScreen(caps = caps)
        ConcernSection.REWARDS -> RewardsScreen(caps = caps, showsNumbers = viewer.level?.showsNumbers ?: true, actions = RewardsActions(onOpenGoal = actions.onOpenGoal))
        ConcernSection.FAMILY_CALENDAR -> FamilyCalendarScreen(caps = caps)
        ConcernSection.FAMILY_TALK -> FamilyTalkScreen(caps = caps)
        ConcernSection.GROWTH_ALBUM -> GrowthAlbumScreen(caps = caps)
        ConcernSection.PERIOD_REPORT -> PeriodReportScreen(caps = caps)
        ConcernSection.BODY -> GrowthScreen(caps = caps)
        ConcernSection.ACTIVITIES -> ActivitiesScreen(caps = caps, actions = ActivitiesActions(onOpenJourney = actions.onOpenJourney))
        ConcernSection.TALENT -> TalentScreen(caps = caps)
    }
}
