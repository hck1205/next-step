package com.nextstep.app.ui.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.project.ProjectCategory
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.card.RoutineCard
import com.nextstep.app.ui.components.input.SegmentedRow
import com.nextstep.app.ui.projects.components.ProjectCard

/**
 * 기록 › 교육 프로젝트 › 진행 중. 학교 공부와 따로, 몇 년에 걸쳐 키우는 힘(영어·독서·악기·운동…)을 분류별로 봅니다.
 * 프로젝트마다 지금 단계 · 이번 주 채운 양 · 계획 대비 속도 · 예상 도착일, 아래에 오늘 루틴 체크.
 */
@Composable
fun ProjectsScreen(actions: ProjectsActions, viewModel: ProjectsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProjectsContent(state = state, actions = actions, onEvent = viewModel::onEvent)
}

@Composable
internal fun ProjectsContent(state: ProjectsUiState, actions: ProjectsActions, onEvent: (ProjectsEvent) -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (state.loaded && state.all.isEmpty()) {
            item {
                AppCard {
                    Column {
                        EmptyState("아직 시작한 교육 프로젝트가 없어요. 영어·독서처럼 오래 키울 힘을 단계와 하루 루틴으로 나눠 드려요.")
                        actions.onBrowse?.let { browse -> TextButton(onClick = browse) { Text("프로젝트 고르기") } }
                    }
                }
            }
            return@LazyColumn
        }
        if (state.categories.size > 1) {
            item {
                SegmentedRow(
                    options = listOf<ProjectCategory?>(null) + state.categories, selected = state.filter,
                    label = { it?.label ?: "전체" }, onSelect = { onEvent(ProjectsEvent.SelectCategory(it)) }, modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        items(state.shown, key = { it.goalId }) { p -> ProjectCard(p, onOpen = { actions.onOpenProject(p.goalId) }) }
        val open = state.shown.filter { !it.isDone }
        if (open.isNotEmpty()) {
            item { Text("오늘의 루틴", style = MaterialTheme.typography.titleSmall) }
            item {
                RoutineCard(
                    items = open, onToggle = { p, item -> onEvent(ProjectsEvent.ToggleRoutine(p, item)) }, onOpen = actions.onOpenProject,
                )
            }
        }
        actions.onBrowse?.let { browse -> item { TextButton(onClick = browse) { Text("다른 프로젝트 고르기") } } }
    }
}
