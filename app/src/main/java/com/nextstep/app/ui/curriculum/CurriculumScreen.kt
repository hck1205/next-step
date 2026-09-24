package com.nextstep.app.ui.curriculum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.curriculum.components.BulletCard
import com.nextstep.app.ui.curriculum.components.PeriodNavRow
import com.nextstep.app.ui.curriculum.components.SubjectPlanCard
import com.nextstep.app.ui.curriculum.components.TermOverviewCard
import androidx.compose.runtime.getValue
import com.nextstep.app.ui.common.ExternalLinks

/**
 * 학기별 교과 커리큘럼. "이 시기 학교에서 이걸 배운다"를 기준선으로 가족의 진도와 대조하고,
 * 단원을 내 과목으로 가져오거나 할 일로 보내고, 저장소 영상이나 유튜브 검색으로 잇습니다.
 */
@Composable
fun CurriculumScreen(caps: Capabilities, actions: CurriculumActions, viewModel: CurriculumViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CurriculumContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CurriculumContent(state: CurriculumUiState, caps: Capabilities, actions: CurriculumActions, onEvent: (CurriculumEvent) -> Unit) {
    val context = LocalContext.current
    val openUrl: (String) -> Unit = { url -> ExternalLinks.open(context, url) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.selected?.let { "${it.label} 커리큘럼" } ?: "교과 커리큘럼") },
                navigationIcon = { if (actions.onBack != null) IconButton(onClick = actions.onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } },
                actions = { TextButton(onClick = actions.onOpenContent) { Text("저장소") } },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                PeriodNavRow(
                    hasPrev = state.hasPrev, hasNext = state.hasNext, isCurrent = state.isCurrent, canJumpToCurrent = state.currentPeriodKey != null,
                    onPrev = { onEvent(CurriculumEvent.PrevPeriod) }, onCurrent = { onEvent(CurriculumEvent.ThisPeriod) }, onNext = { onEvent(CurriculumEvent.NextPeriod) },
                )
            }
            val plan = state.plan
            if (state.loaded && plan == null) {
                item { AppCard { EmptyState(if (state.periods.isEmpty()) "가족 탭에서 자녀의 생년월일이나 학년을 입력하면 학기 커리큘럼이 보여요" else "이 구간에는 교과 커리큘럼이 없어요 (초1~고3)") } }
                return@LazyColumn
            }
            if (plan == null) return@LazyColumn
            item { TermOverviewCard(plan) }
            items(plan.subjects, key = { it.subject }) { sp ->
                SubjectPlanCard(
                    sp, caps, onOpenSubject = actions.onOpenSubject,
                    onImport = { onEvent(CurriculumEvent.ImportSubject(sp.subject)) },
                    onAddTask = { onEvent(CurriculumEvent.AddTask(it, caps.actingRoleName)) },
                    onOpenUrl = openUrl, onWatched = { onEvent(CurriculumEvent.MarkWatched(it)) },
                )
            }
            if (plan.peerExtras.isNotEmpty()) {
                item { SectionTitle("다른 가족들이 이 시기에 더 배운 것") }
                item { BulletCard(plan.peerExtras.map { "${it.subject} · ${it.title} · ${it.families}가족" }, footnote = "참고용이에요. 우리 아이 진도가 기준입니다.", bullet = "") }
            }
            if (state.nextPreview.isNotEmpty()) {
                item { SectionTitle("다음 학기 미리 보기 · 뼈대 단원") }
                item { BulletCard(state.nextPreview) }
            }
            item {
                Text("교육과정(2022 개정) 기준이며 교과서에 따라 순서가 조금 다를 수 있어요. 선행이 아니라 학교 진도가 기준입니다.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
