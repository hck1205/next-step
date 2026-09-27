package com.nextstep.app.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyCard
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.dialog.SubjectEditDialog
import com.nextstep.app.ui.components.layout.ScreenPadding
import com.nextstep.app.ui.components.layout.hostedSectionAdd
import com.nextstep.app.ui.progress.components.ReviewGapCard
import com.nextstep.app.ui.progress.components.SubjectProgressCard

@Composable
fun ProgressScreen(caps: Capabilities, actions: ProgressActions, viewModel: ProgressViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProgressContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProgressContent(state: ProgressUiState, caps: Capabilities, actions: ProgressActions, onEvent: (ProgressEvent) -> Unit) {
    var showAdd by remember { mutableStateOf(false) }
    // 기록 탭 안에서는 "만들기"가 상단 바로 올라가고, 따로 열었을 때만 + 버튼을 그립니다.
    val hosted = hostedSectionAdd(if (caps.canEditSubjects) "과목 추가" else null) { showAdd = true }

    Scaffold(
        floatingActionButton = {
            if (!hosted && caps.canEditSubjects) FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, contentDescription = "과목 추가") }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = ScreenPadding.list,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (caps.isStudent) item {
                AppCard {
                    Text(
                        "지금 배우는 과목의 단원을 등록하고, 수업이 어디까지 나갔는지 표시하면 예습·복습할 내용이 자동으로 정해져요. 오늘 화면 ⋮ 메뉴의 '학습 계획 만들기'로 캘린더에 배치할 수 있어요.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (state.progress.any { it.classCovered > 0 }) {
                item { SectionTitle("수업한 단원 대비 복습") }
                item { ReviewGapCard(state.progress) }
            }
            // 로드맵은 같은 기록 탭의 배울 것 › 로드맵에 있어 여기서 다시 잇지 않습니다(본문에는 내용만).
            item { SectionTitle(if (caps.isStudent) "내 커리큘럼" else "과목별 진도") }
            if (state.progress.isEmpty()) item { EmptyCard(if (caps.canEditSubjects) "과목을 추가하고 단원을 등록해 보세요" else "아직 등록된 과목이 없어요") }
            items(state.progress, key = { it.subject.id }) { p -> SubjectProgressCard(p, onClick = { actions.onOpenSubject(p.subject.id) }) }
        }
    }

    if (showAdd) {
        SubjectEditDialog(null, onDismiss = { showAdd = false }) { name, color, goal, teacher ->
            onEvent(ProgressEvent.AddSubject(name, color, goal, teacher))
        }
    }
}
