package com.nextstep.app.ui.hub

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.domain.hub.ConcernSection
import com.nextstep.app.domain.hub.HubViewer
import com.nextstep.app.ui.activities.ActivitiesActions
import com.nextstep.app.ui.assignments.AssignmentsScreen
import com.nextstep.app.ui.review.ReviewScreen
import com.nextstep.app.ui.habits.HabitsScreen
import com.nextstep.app.ui.activities.ActivitiesScreen
import com.nextstep.app.ui.calendar.CalendarScreen
import com.nextstep.app.ui.components.input.SegmentedRow
import com.nextstep.app.ui.content.ContentActions
import com.nextstep.app.ui.content.ContentLibraryScreen
import com.nextstep.app.ui.curriculum.CurriculumActions
import com.nextstep.app.ui.curriculum.CurriculumScreen
import com.nextstep.app.ui.goals.GoalsActions
import com.nextstep.app.ui.goals.GoalsScreen
import com.nextstep.app.ui.grades.GradesScreen
import com.nextstep.app.ui.growth.GrowthScreen
import com.nextstep.app.ui.goaltree.GoalTreeActions
import com.nextstep.app.ui.goaltree.GoalTreeScreen
import com.nextstep.app.ui.planhistory.PlanHistoryActions
import com.nextstep.app.ui.planhistory.PlanHistoryScreen
import com.nextstep.app.ui.rewards.RewardsActions
import com.nextstep.app.ui.rewards.RewardsScreen
import com.nextstep.app.ui.todo.TodoActions
import com.nextstep.app.ui.todo.TodoScreen
import com.nextstep.app.ui.hub.components.ConcernTabs
import com.nextstep.app.ui.insights.InsightsActions
import com.nextstep.app.ui.insights.InsightsScreen
import com.nextstep.app.ui.overview.OverviewActions
import com.nextstep.app.ui.overview.OverviewScreen
import com.nextstep.app.ui.progress.ProgressActions
import com.nextstep.app.ui.progress.ProgressScreen
import com.nextstep.app.ui.projectcatalog.ProjectCatalogActions
import com.nextstep.app.ui.projectcatalog.ProjectCatalogScreen
import com.nextstep.app.ui.projects.ProjectsActions
import com.nextstep.app.ui.projects.ProjectsScreen
import com.nextstep.app.ui.roadmap.RoadmapActions
import com.nextstep.app.ui.roadmap.RoadmapScreen
import com.nextstep.app.ui.selfdirection.SelfDirectionActions
import com.nextstep.app.ui.selfdirection.SelfDirectionScreen
import com.nextstep.app.ui.talent.TalentScreen
import kotlinx.coroutines.launch

/**
 * 기록 탭(학생은 "나"): 기능을 관심사별로 나눈 두 단 구조입니다.
 * 위 줄 = 관심사(한눈에 · 목표·할 일 · 공부 · 배울 것 · 교육 프로젝트 · 시험·성적 · 성장 · 활동·재능), 옆으로 밀어서도 넘깁니다.
 * 순서는 보는 자리가 정합니다: 학생은 배울 것, 멘토는 과제가 한눈에 바로 다음(HubAudience).
 * 아래 줄 = 그 관심사의 섹션(예: 공부 › 스스로 · 진도 · 시간 · 습관 · 일정). 섹션 하나가 기능 화면 하나이고 각자 ViewModel 을 가집니다.
 * 어떤 섹션이 보이는지는 domain/hub/ConcernSection 이 정합니다(학생은 화면 단계에 따라 줄고, 멘토에게 신체 기록은 없음).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HubScreen(caps: Capabilities, studentLevel: StudentUiLevel?, actions: HubActions, initialSection: ConcernSection) {
    val viewer = remember(caps, studentLevel) { HubViewer.of(caps, studentLevel) }
    val concerns = remember(viewer) { ConcernSection.concernsFor(viewer) }
    val pager = rememberPagerState(initialPage = concerns.indexOf(initialSection.concern).coerceAtLeast(0)) { concerns.size }
    val chosen = remember { mutableStateMapOf(initialSection.concern to initialSection) }
    val scope = rememberCoroutineScope()
    val open: (ConcernSection) -> Unit = { section ->
        val page = concerns.indexOf(section.concern)
        if (page >= 0 && section.visibleFor(viewer)) {
            chosen[section.concern] = section
            scope.launch { pager.animateScrollToPage(page) }
        }
    }
    val openConcern: (Concern) -> Unit = { concern -> concerns.indexOf(concern).takeIf { it >= 0 }?.let { page -> scope.launch { pager.animateScrollToPage(page) } } }

    Scaffold(topBar = { TopAppBar(title = { Text(if (caps.isStudent) "나" else "기록") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ConcernTabs(concerns, selected = pager.currentPage, onSelect = { page -> scope.launch { pager.animateScrollToPage(page) } })
            HorizontalPager(state = pager, modifier = Modifier.fillMaxSize(), key = { concerns[it].name }) { page ->
                val concern = concerns[page]
                val sections = ConcernSection.sectionsOf(concern, viewer)
                val section = chosen[concern]?.takeIf { it in sections } ?: sections.first()
                Column(Modifier.fillMaxSize()) {
                    if (sections.size > 1) {
                        SegmentedRow(
                            options = sections, selected = section, label = { it.label }, onSelect = { chosen[concern] = it },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }
                    Box(Modifier.fillMaxSize()) { SectionContent(section, caps, viewer, concerns, actions, open, openConcern) }
                }
            }
        }
    }
}

/** 섹션 → 기능 화면. 기능 화면은 onBack 없이 그려져 제목줄 대신 관심사·섹션 줄을 씁니다. */
@Composable
private fun SectionContent(section: ConcernSection, caps: Capabilities, viewer: HubViewer, concerns: List<Concern>, actions: HubActions, open: (ConcernSection) -> Unit, openConcern: (Concern) -> Unit) {
    when (section) {
        ConcernSection.OVERVIEW -> OverviewScreen(concerns = concerns, actions = OverviewActions(onOpenConcern = openConcern))
        ConcernSection.SELF -> SelfDirectionScreen(caps = caps, actions = SelfDirectionActions(onOpenProjects = { open(ConcernSection.PROJECTS) }))
        ConcernSection.PROGRESS -> ProgressScreen(caps = caps, actions = ProgressActions(onOpenSubject = actions.onOpenSubject, onOpenRoadmap = { open(ConcernSection.ROADMAP) }))
        ConcernSection.TIME -> InsightsScreen(caps = caps, actions = InsightsActions())
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
        ConcernSection.REWARDS -> RewardsScreen(caps = caps, showsNumbers = viewer.level?.showsNumbers ?: true, actions = RewardsActions(onOpenGoal = actions.onOpenGoal))
        ConcernSection.BODY -> GrowthScreen(caps = caps)
        ConcernSection.ACTIVITIES -> ActivitiesScreen(caps = caps, actions = ActivitiesActions(onOpenJourney = actions.onOpenJourney))
        ConcernSection.TALENT -> TalentScreen(caps = caps)
    }
}
