package com.nextstep.app.ui.feedback

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
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.row.NoteRow

/** 과제·피드백 › 피드백: 요약 한 장 → 주마다 묶은 메모(머리는 위에 붙어 따라옵니다). */
@Composable
fun FeedbackScreen(viewModel: FeedbackViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FeedbackContent(state)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FeedbackContent(state: FeedbackUiState) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (state.thisWeekCount == 0) "이번 주 피드백은 아직 없어요" else "이번 주 피드백 ${state.thisWeekCount}개", style = MaterialTheme.typography.titleSmall)
                    Text("멘토 ${state.fromMentors} · 가족 ${state.fromFamily} · + 버튼으로 남겨요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (state.loaded && state.weeks.isEmpty()) item { AppCard { EmptyState("남긴 피드백이 여기에 주마다 쌓여요.") } }
        state.weeks.forEach { week ->
            stickyHeader(key = "w${week.label}") {
                Text(
                    "${week.label} · ${week.notes.size}", style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(vertical = 6.dp),
                )
            }
            items(week.notes, key = { it.id }) { NoteRow(it, onDelete = null, showTime = true) }
        }
    }
}
