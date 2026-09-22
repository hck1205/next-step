package com.nextstep.app.ui.content

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.local.ContentEntity
import com.nextstep.app.data.model.ContentScope
import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.domain.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AdBanner
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.OptionPicker
import com.nextstep.app.ui.components.SectionTitle

/**
 * 교육 콘텐츠 저장소. 유튜브 링크를 등록하면 자동 분류되고, 학생의 진도·약점에 맞춰 추천됩니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentLibraryScreen(caps: Capabilities, onBack: (() -> Unit)?, viewModel: ContentViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ContentEntity?>(null) }
    val context = LocalContext.current
    val open: (ContentEntity) -> Unit = { c -> runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(c.url))) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("콘텐츠 저장소") },
                navigationIcon = { if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } },
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = { viewModel.resetAdd(); showAdd = true }) { Icon(Icons.Default.Add, contentDescription = "링크 등록") } },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.recommendations.isNotEmpty() && state.filter == ContentFilter()) {
                item { SectionTitle(if (caps.isStudent) "지금 나에게 맞는 영상" else "학생에게 추천할 만한 영상") }
                items(state.recommendations, key = { "rec" + it.content.id }) { rec ->
                    ContentRow(rec.content, reason = rec.reason, caps = caps, onOpen = { open(rec.content) }, onRate = { viewModel.rate(rec.content.id, it) }, onWatched = { viewModel.setWatched(rec.content.id, it) }, onEdit = { editing = rec.content })
                }
            }

            item {
                SectionTitle("전체 ${state.all.size}개")
                OutlinedTextField(value = state.filter.query, onValueChange = viewModel::setQuery, label = { Text("제목·채널·키워드 검색") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = state.filter.subjectKey == null, onClick = { viewModel.setSubject(null) }, label = { Text("모든 과목") })
                    state.subjectKeys.forEach { k -> FilterChip(selected = state.filter.subjectKey == k, onClick = { viewModel.setSubject(if (state.filter.subjectKey == k) null else k) }, label = { Text(k) }) }
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ContentType.entries.forEach { t -> FilterChip(selected = state.filter.type == t, onClick = { viewModel.setType(if (state.filter.type == t) null else t) }, label = { Text(t.label) }) }
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(GradeLevel.ELEMENTARY, GradeLevel.MIDDLE, GradeLevel.HIGH).forEach { l -> FilterChip(selected = state.filter.level == l, onClick = { viewModel.setLevel(if (state.filter.level == l) null else l) }, label = { Text(l.label) }) }
                    if (caps.isStudent) FilterChip(selected = state.filter.hideWatched, onClick = viewModel::toggleHideWatched, label = { Text("본 영상 숨기기") })
                }
            }

            if (state.filtered.isEmpty()) item { AppCard { EmptyState(if (state.all.isEmpty()) "첫 유튜브 링크를 등록해 보세요. 제목을 읽어 과목·학년·유형을 자동으로 분류해요." else "조건에 맞는 콘텐츠가 없어요") } }
            items(state.filtered, key = { it.id }) { c ->
                ContentRow(c, reason = null, caps = caps, onOpen = { open(c) }, onRate = { viewModel.rate(c.id, it) }, onWatched = { viewModel.setWatched(c.id, it) }, onEdit = { editing = c })
            }
            if (!caps.isStudent) item { AdBanner() }
        }
    }

    if (showAdd) {
        AddContentDialog(state, viewModel, subjectKeys = state.subjectKeys, onDismiss = { showAdd = false; viewModel.resetAdd() })
    }
    editing?.let { c ->
        EditContentDialog(c, state.subjectKeys, canDelete = c.scope == ContentScope.FAMILY && (caps.actsAsMentor || caps.isStudent), onDismiss = { editing = null },
            onDelete = { viewModel.delete(c.id); editing = null }) { viewModel.update(it); editing = null }
    }
}

@Composable
fun ContentRow(c: ContentEntity, reason: String?, caps: Capabilities, onOpen: () -> Unit, onRate: (Int) -> Unit, onWatched: (Boolean) -> Unit, onEdit: () -> Unit) {
    AppCard(onClick = onOpen) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (reason != null) Text(reason, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(c.title.ifBlank { c.url }, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        listOf(c.channel, c.subjectKey, c.gradeLevel.takeIf { it != GradeLevel.ALL }?.label, c.contentType.label, c.durationMinutes.takeIf { it > 0 }?.let { "${it}분" })
                            .filterNotNull().filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (c.scope == ContentScope.GLOBAL) Text("공용", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
            }
            if (c.summary.isNotBlank()) Text(c.summary, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (c.keywordList.isNotEmpty()) Text(c.keywordList.joinToString("  ") { "#$it" }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RatingStars(c.averageRating, enabled = c.scope == ContentScope.FAMILY, onRate = onRate)
                if (c.ratingCount > 0) Text(" ${c.ratingCount}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                if (caps.isStudent) TextButton(onClick = { onWatched(!c.watched) }) { Text(if (c.watched) "다시 보기" else "봤어요") }
                TextButton(onClick = onEdit) { Text("정보") }
            }
        }
    }
}

@Composable
private fun RatingStars(value: Float, enabled: Boolean, onRate: (Int) -> Unit) {
    Row {
        (1..5).forEach { i ->
            IconButton(onClick = { onRate(i) }, enabled = enabled, modifier = Modifier.width(24.dp).height(24.dp)) {
                Icon(if (value >= i - 0.5f) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = "$i 점", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.width(18.dp))
            }
        }
    }
}

@Composable
private fun AddContentDialog(state: ContentUiState, viewModel: ContentViewModel, subjectKeys: List<String>, onDismiss: () -> Unit) {
    val add = state.add
    val draft = add.draft
    var title by remember(draft) { mutableStateOf(draft?.title ?: "") }
    var channel by remember(draft) { mutableStateOf(draft?.channel ?: "") }
    var subjectKey by remember(draft) { mutableStateOf(draft?.classification?.subjectKey ?: "") }
    var level by remember(draft) { mutableStateOf(draft?.classification?.gradeLevel ?: GradeLevel.ALL) }
    var type by remember(draft) { mutableStateOf(draft?.classification?.contentType ?: ContentType.OTHER) }
    var keywords by remember(draft) { mutableStateOf(draft?.classification?.keywords?.joinToString(", ") ?: "") }
    var summary by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (draft == null) "유튜브 링크 등록" else "분류 확인") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (draft == null) {
                    OutlinedTextField(value = add.url, onValueChange = viewModel::setUrl, label = { Text("유튜브 링크 붙여넣기") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text("영상은 저장하지 않고 링크만 등록해요. 제목과 채널을 읽어 과목·학년·유형·키워드를 자동으로 붙입니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    add.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    if (add.loading) CircularProgressIndicator(Modifier.width(24.dp).height(24.dp))
                } else {
                    if (!draft.metadataFetched) Text("제목을 자동으로 가져오지 못했어요. 직접 입력해 주세요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("제목") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = channel, onValueChange = { channel = it }, label = { Text("채널") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    SubjectKeyPicker(subjectKeys, subjectKey) { subjectKey = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OptionPicker(GradeLevel.entries, level, label = { it.label }, onSelect = { level = it }, modifier = Modifier.weight(1f))
                        OptionPicker(ContentType.entries, type, label = { it.label }, onSelect = { type = it }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = keywords, onValueChange = { keywords = it }, label = { Text("키워드 (쉼표 구분, 단원명 등)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("한 줄 설명 (선택)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = minutes, onValueChange = { minutes = it.filter { c -> c.isDigit() }.take(3) }, label = { Text("길이(분, 선택)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    if (draft.classification.reasons.isNotEmpty()) Text("자동 분류 근거: " + draft.classification.reasons.joinToString(" / "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            if (draft == null) Button(onClick = viewModel::analyze, enabled = add.url.isNotBlank() && !add.loading) { Text("분석") }
            else TextButton(enabled = title.isNotBlank(), onClick = { viewModel.save(title, channel, subjectKey, level, type, keywords, summary, minutes.toIntOrNull() ?: 0); onDismiss() }) { Text("등록") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

@Composable
private fun EditContentDialog(c: ContentEntity, subjectKeys: List<String>, canDelete: Boolean, onDismiss: () -> Unit, onDelete: () -> Unit, onSave: (ContentEntity) -> Unit) {
    val editable = c.scope == ContentScope.FAMILY
    var title by remember { mutableStateOf(c.title) }
    var subjectKey by remember { mutableStateOf(c.subjectKey) }
    var level by remember { mutableStateOf(c.gradeLevel) }
    var type by remember { mutableStateOf(c.contentType) }
    var keywords by remember { mutableStateOf(c.keywords) }
    var summary by remember { mutableStateOf(c.summary) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editable) "콘텐츠 정보 수정" else "콘텐츠 정보") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(c.url, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                if (c.createdByName.isNotBlank()) Text("등록: ${c.createdByName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("제목") }, enabled = editable, modifier = Modifier.fillMaxWidth())
                if (editable) SubjectKeyPicker(subjectKeys, subjectKey) { subjectKey = it } else Text("과목: ${c.subjectKey.ifBlank { "-" }}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OptionPicker(GradeLevel.entries, level, label = { it.label }, onSelect = { if (editable) level = it }, modifier = Modifier.weight(1f))
                    OptionPicker(ContentType.entries, type, label = { it.label }, onSelect = { if (editable) type = it }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = keywords, onValueChange = { keywords = it }, label = { Text("키워드") }, enabled = editable, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("설명") }, enabled = editable, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            if (editable) TextButton(enabled = title.isNotBlank(), onClick = { onSave(c.copy(title = title.trim(), subjectKey = subjectKey.trim(), gradeLevel = level, contentType = type, keywords = keywords, summary = summary.trim())) }) { Text("저장") }
            else TextButton(onClick = onDismiss) { Text("닫기") }
        },
        dismissButton = {
            Row {
                if (canDelete) TextButton(onClick = onDelete) { Text("삭제", color = MaterialTheme.colorScheme.error) }
                if (editable) TextButton(onClick = onDismiss) { Text("취소") }
            }
        },
    )
}

/** 과목 키 선택: 기존 키 칩 + 직접 입력. */
@Composable
private fun SubjectKeyPicker(keys: List<String>, value: String, onChange: (String) -> Unit) {
    Column {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            keys.forEach { k -> FilterChip(selected = value == k, onClick = { onChange(if (value == k) "" else k) }, label = { Text(k) }) }
        }
        OutlinedTextField(value = value, onValueChange = onChange, label = { Text("과목") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    }
}
