package com.nextstep.app.ui.assignments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.assignments.components.AssignmentRow
import com.nextstep.app.ui.assignments.components.AssignmentSubjectsCard
import com.nextstep.app.ui.assignments.components.AssignmentSummaryCard
import com.nextstep.app.ui.components.card.SectionTitle

/** 과제·피드백 › 과제: 요약 → 밀린 과제 → 이번 주 마감 → 과목별 완료율 → 최근 끝낸 과제. */
@Composable
fun AssignmentsScreen(viewModel: AssignmentsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AssignmentsContent(state)
}

@Composable
internal fun AssignmentsContent(state: AssignmentsUiState) {
    val r = state.report ?: return
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { AssignmentSummaryCard(r) }
        if (r.overdue.isNotEmpty()) {
            item { SectionTitle("밀린 과제") }
            items(r.overdue, key = { "o-${it.id}" }) { AssignmentRow(it, state.subjects, state.today) }
        }
        if (r.dueSoon.isNotEmpty()) {
            item { SectionTitle("이번 주 마감") }
            items(r.dueSoon, key = { "s-${it.id}" }) { AssignmentRow(it, state.subjects, state.today) }
        }
        if (r.bySubject.isNotEmpty()) item { AssignmentSubjectsCard(r.bySubject) }
        if (r.recentDone.isNotEmpty()) {
            item { SectionTitle("최근 끝낸 과제") }
            items(r.recentDone, key = { "d-${it.id}" }) { AssignmentRow(it, state.subjects, state.today) }
        }
    }
}
