package com.nextstep.app.ui.content

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.model.ContentScope
import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AdBanner
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.content.components.AddContentDialog
import com.nextstep.app.ui.content.components.ContentRow
import com.nextstep.app.ui.content.components.EditContentDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.nextstep.app.ui.common.ExternalLinks

/**
 * 교육 콘텐츠 저장소. 유튜브 링크를 등록하면 자동 분류되고, 학생의 진도·약점에 맞춰 추천됩니다.
 */
@Composable
fun ContentLibraryScreen(caps: Capabilities, actions: ContentActions, viewModel: ContentViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ContentContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContentContent(state: ContentUiState, caps: Capabilities, actions: ContentActions, onEvent: (ContentEvent) -> Unit) {
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ContentEntity?>(null) }
    val context = LocalContext.current
    val open: (ContentEntity) -> Unit = { c -> ExternalLinks.open(context, c.url) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("콘텐츠 저장소") },
                navigationIcon = { if (actions.onBack != null) IconButton(onClick = actions.onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } },
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = { onEvent(ContentEvent.ResetAdd); showAdd = true }) { Icon(Icons.Default.Add, contentDescription = "링크 등록") } },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.recommendations.isNotEmpty() && state.filter == ContentFilter()) {
                item { SectionTitle(if (caps.isStudent) "지금 나에게 맞는 영상" else "학생에게 추천할 만한 영상") }
                items(state.recommendations, key = { "rec" + it.content.id }) { rec ->
                    ContentRow(rec.content, reason = rec.reason, caps = caps, onOpen = { open(rec.content) }, onRate = { onEvent(ContentEvent.Rate(rec.content.id, it)) }, onWatched = { onEvent(ContentEvent.SetWatched(rec.content.id, it)) }, onEdit = { editing = rec.content })
                }
            }

            item {
                SectionTitle("전체 ${state.all.size}개")
                OutlinedTextField(value = state.filter.query, onValueChange = { onEvent(ContentEvent.SetQuery(it)) }, label = { Text("제목·채널·키워드 검색") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = state.filter.subjectKey == null, onClick = { onEvent(ContentEvent.SetSubject(null)) }, label = { Text("모든 과목") })
                    state.subjectKeys.forEach { k -> FilterChip(selected = state.filter.subjectKey == k, onClick = { onEvent(ContentEvent.SetSubject(if (state.filter.subjectKey == k) null else k)) }, label = { Text(k) }) }
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ContentType.entries.forEach { t -> FilterChip(selected = state.filter.type == t, onClick = { onEvent(ContentEvent.SetType(if (state.filter.type == t) null else t)) }, label = { Text(t.label) }) }
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(GradeLevel.ELEMENTARY, GradeLevel.MIDDLE, GradeLevel.HIGH).forEach { l -> FilterChip(selected = state.filter.level == l, onClick = { onEvent(ContentEvent.SetLevel(if (state.filter.level == l) null else l)) }, label = { Text(l.label) }) }
                    if (caps.isStudent) FilterChip(selected = state.filter.hideWatched, onClick = { onEvent(ContentEvent.ToggleHideWatched) }, label = { Text("본 영상 숨기기") })
                }
            }

            if (state.filtered.isEmpty()) item { AppCard { EmptyState(if (state.all.isEmpty()) "첫 유튜브 링크를 등록해 보세요. 제목을 읽어 과목·학년·유형을 자동으로 분류해요." else "조건에 맞는 콘텐츠가 없어요") } }
            items(state.filtered, key = { it.id }) { c ->
                ContentRow(c, reason = null, caps = caps, onOpen = { open(c) }, onRate = { onEvent(ContentEvent.Rate(c.id, it)) }, onWatched = { onEvent(ContentEvent.SetWatched(c.id, it)) }, onEdit = { editing = c })
            }
            if (!caps.isStudent) item { AdBanner() }
        }
    }

    if (showAdd) {
        AddContentDialog(state, onEvent, subjectKeys = state.subjectKeys, onDismiss = { showAdd = false; onEvent(ContentEvent.ResetAdd) })
    }
    editing?.let { c ->
        EditContentDialog(c, state.subjectKeys, canDelete = c.scope == ContentScope.FAMILY && (caps.actsAsMentor || caps.isStudent), onDismiss = { editing = null },
            onDelete = { onEvent(ContentEvent.Delete(c.id)); editing = null }) { onEvent(ContentEvent.Update(it)); editing = null }
    }
}
