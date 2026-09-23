package com.nextstep.app.ui.activities

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.model.ActivityType
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.activities.components.ActivityEditDialog
import com.nextstep.app.ui.activities.components.ActivityRow
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.card.SectionTitle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * 활동 기록. 취미·동아리·현장학습·체험·봉사·대회·여행을 구간(학기)별로 남깁니다.
 * 쌓인 기록이 포트폴리오·생기부·자기소개서의 재료가 됩니다.
 */
@Composable
fun ActivitiesScreen(caps: Capabilities, actions: ActivitiesActions, viewModel: ActivitiesViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ActivitiesContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ActivitiesContent(state: ActivitiesUiState, caps: Capabilities, actions: ActivitiesActions, onEvent: (ActivitiesEvent) -> Unit) {
    var editing by remember { mutableStateOf<ActivityEntity?>(null) }
    var showEdit by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.studentName.isBlank()) "활동 기록" else "${state.studentName}의 활동 기록") },
                navigationIcon = { if (actions.onBack != null) IconButton(onClick = actions.onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } },
                actions = { TextButton(onClick = actions.onOpenJourney) { Text("타임라인") } },
            )
        },
        floatingActionButton = {
            if (caps.canRecordActivities) FloatingActionButton(onClick = { editing = null; showEdit = true }) { Icon(Icons.Default.Add, contentDescription = "활동 추가") }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                AppCard {
                    Column {
                        Text(
                            if (state.activities.isEmpty()) "첫 활동을 기록해 보세요. 현장학습 한 번, 취미 시작도 좋아요."
                            else "총 ${state.activities.size}개 · 이번 구간 ${state.currentPeriodCount}개 · 진행 중인 취미·동아리 ${state.ongoing.size}개",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (state.countByType.isNotEmpty()) Text(
                            state.countByType.entries.joinToString(" · ") { "${it.key.label} ${it.value}" },
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = state.filter == null, onClick = { onEvent(ActivitiesEvent.SetFilter(null)) }, label = { Text("전체") })
                    ActivityType.entries.forEach { t ->
                        FilterChip(selected = state.filter == t, onClick = { onEvent(ActivitiesEvent.SetFilter(if (state.filter == t) null else t)) }, label = { Text(t.label) })
                    }
                }
            }
            if (state.loaded && state.filtered.isEmpty()) item { AppCard { EmptyState("기록된 활동이 없어요") } }
            state.sections.forEach { (label, group) ->
                item { SectionTitle("$label · ${group.size}") }
                items(group, key = { it.id }) { activity ->
                    ActivityRow(
                        activity = activity,
                        onEdit = if (caps.canRecordActivities) ({ editing = activity; showEdit = true }) else null,
                        onDelete = if (caps.canRecordActivities) ({ onEvent(ActivitiesEvent.Delete(activity.id)) }) else null,
                    )
                }
            }
        }
    }

    if (showEdit) ActivityEditDialog(
        existing = editing, today = state.today,
        onConfirm = { onEvent(ActivitiesEvent.Save(it)); showEdit = false },
        onDismiss = { showEdit = false },
    )
}
