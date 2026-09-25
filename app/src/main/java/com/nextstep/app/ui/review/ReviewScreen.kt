package com.nextstep.app.ui.review

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.review.components.ReviewRow

/** 배울 것 › 복습: 과목별 남은 단원 한 줄 → 이유별 목록(이해도 낮음 → 점수 내려간 과목 → 수업 뒤 → 다음 수업 예습). */
@Composable
fun ReviewScreen(caps: Capabilities, viewModel: ReviewViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReviewContent(state = state, caps = caps, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ReviewContent(state: ReviewUiState, caps: Capabilities, onEvent: (ReviewEvent) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (state.total == 0) "지금 다시 볼 단원이 없어요" else "다시 볼 단원 ${state.total}개", style = MaterialTheme.typography.titleSmall)
                    if (state.perSubject.isNotEmpty()) {
                        Text(state.perSubject.joinToString(" · ") { (name, n) -> "$name $n" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (state.loaded && state.total == 0) item { AppCard { EmptyState("수업한 단원을 진도에 표시하면 복습할 단원이 여기에 모여요.") } }
        state.sections.forEach { (reason, rows) ->
            stickyHeader(key = "r${reason.name}") {
                Text(
                    "${reason.label} · ${rows.size}", style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(vertical = 6.dp),
                )
            }
            items(rows, key = { "${reason.name}-${it.topic.id}" }) { item ->
                ReviewRow(
                    item = item,
                    onAddTask = if (caps.canCreateTasks) ({ onEvent(ReviewEvent.AddTask(item, caps.actingRoleName)) }) else null,
                    onDone = if (caps.canMarkTopicStatus) ({ onEvent(ReviewEvent.MarkDone(item)) }) else null,
                )
            }
        }
    }
}
