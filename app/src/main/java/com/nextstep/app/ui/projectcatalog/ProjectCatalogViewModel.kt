package com.nextstep.app.ui.projectcatalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.GoalRepository
import com.nextstep.app.domain.family.StudentContext
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.project.ProjectCatalog
import com.nextstep.app.domain.project.ProjectCategory
import com.nextstep.app.domain.project.ProjectKind
import com.nextstep.app.domain.project.ProjectPlanner
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** 기록 › 교육 프로젝트 › 새로 시작: 나이에 맞는 프로젝트와 시작 단계를 추천하고, 고른 단계부터 일정을 만들어 저장합니다. */
class ProjectCatalogViewModel(
    streams: FamilyDataStreams,
    private val goals: GoalRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val filter = MutableStateFlow<ProjectCategory?>(null)

    val state: StateFlow<ProjectCatalogUiState> = combine(streams.profile, streams.members, streams.goals, filter) { profile, members, goals, f ->
        val day = today()
        val ctx = StudentContext.of(members, day)
        val age = ProjectPlanner.ageMonths(ctx.birthDate, ctx.student?.gradeYear, day)
        val started = goals.filter { !it.deleted && ProjectPlanner.isProject(it) }.mapNotNull { ProjectPlanner.planOf(it)?.id }.toSet()
        val plans = ProjectPlanner.recommend(ProjectCatalog.plans, age, emptySet())
        ProjectCatalogUiState(
            loaded = true, plans = plans, suggested = plans.associate { it.id to ProjectPlanner.suggestedStart(it, age) },
            started = started, filter = f, ageLabel = ctx.birthDate?.let { GrowthStage.ageLabel(it, day) } ?: ctx.gradeLabel, today = day,
            startKind = ProjectKind.startedBy(profile.role),
        )
    }.asUiState(viewModelScope, ProjectCatalogUiState())

    fun onEvent(event: ProjectCatalogEvent) {
        when (event) {
            is ProjectCatalogEvent.SelectCategory -> filter.value = event.category
            is ProjectCatalogEvent.Start -> start(event.planId, event.startIndex)
        }
    }

    /** 같은 프로젝트를 두 번 시작하지 않습니다. */
    private fun start(planId: String, startIndex: Int) {
        viewModelScope.launch {
            val plan = ProjectCatalog.byId[planId] ?: return@launch
            val s = state.value
            if (planId in s.started) return@launch
            val (goal, steps) = ProjectPlanner.start(plan, startIndex, today(), s.startKind.assigner.role.name)
            goals.add(goal, steps)
        }
    }
}
