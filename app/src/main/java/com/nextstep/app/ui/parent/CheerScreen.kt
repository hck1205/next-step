package com.nextstep.app.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.BarChart
import com.nextstep.app.ui.components.BarItem
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.SectionTitle
import com.nextstep.app.ui.components.StatTile
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/** 학부모 전용: 자녀에게 격려 메시지를 보내고, 오늘의 성취를 근거로 칭찬을 돕는 화면. */
@Composable
fun CheerScreen(viewModel: CheerViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CheerContent(state = state, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CheerContent(state: CheerUiState, onEvent: (CheerEvent) -> Unit) {
    var text by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("${state.studentName.ifBlank { "자녀" }} 응원하기") }) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("연속 학습", "${state.streak}일", Modifier.weight(1f), tint = MaterialTheme.colorScheme.error, icon = Icons.Default.LocalFireDepartment)
                    StatTile("오늘 공부", DateUtils.formatMinutes(state.todayMinutes), Modifier.weight(1f))
                    StatTile("오늘 완료", "${state.todayDoneTasks}개", Modifier.weight(1f), tint = MaterialTheme.colorScheme.secondary, sub = if (state.todayTopicsReviewed > 0) "복습 ${state.todayTopicsReviewed}단원" else null)
                }
            }
            item {
                SectionTitle("이번 주 흐름")
                AppCard {
                    BarChart(
                        items = state.daily.map { d -> BarItem(DateUtils.dayOfWeekLabel(d.date.dayOfWeek), d.minutes.toFloat(), MaterialTheme.colorScheme.secondary) },
                        valueFormatter = { DateUtils.formatMinutes(it.toInt()) }, height = 130,
                    )
                }
            }

            item {
                SectionTitle("오늘 기록에 맞춘 칭찬 (눌러서 보내기)")
                AppCard {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(state.praiseStyle?.let { "이 시기의 칭찬은 $it" } ?: "결과보다 과정을, 막연한 칭찬보다 구체적인 칭찬을 권해요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        state.cheerSuggestions.forEach { s ->
                            AssistChip(onClick = { onEvent(CheerEvent.Send(s)) }, label = { Text(s) }, leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.error) })
                        }
                    }
                }
            }

            item {
                SectionTitle("직접 쓰기")
                AppCard {
                    Column {
                        OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("응원 메시지") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { onEvent(CheerEvent.Send(text)); text = "" }, enabled = text.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null); Spacer(Modifier.padding(4.dp)); Text("보내기")
                        }
                    }
                }
            }

            item { SectionTitle("주고받은 메시지") }
            if (state.notes.isEmpty()) item { AppCard { EmptyState("첫 응원 메시지를 보내 보세요") } }
            else items(state.notes, key = { it.id }) { n ->
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(n.text, style = MaterialTheme.typography.bodyLarge)
                            Text("${n.authorName} (${Role.labelOf(n.authorRole)}) · ${DateUtils.formatDate(DateUtils.toLocalDate(n.createdAt))} ${DateUtils.formatTime(n.createdAt)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (n.authorName == state.myName && n.authorRole == Role.PARENT.name) TextButton(onClick = { onEvent(CheerEvent.Delete(n.id)) }) { Text("삭제") }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
