package com.nextstep.app.ui.lessons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.lesson.LessonStatus
import com.nextstep.app.domain.plan.Feature
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.EmptyCard
import com.nextstep.app.ui.components.dialog.LessonPlanDialog
import com.nextstep.app.ui.components.input.DateField
import com.nextstep.app.ui.lessons.components.LessonBookCard
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun LessonsScreen(caps: Capabilities, viewModel: LessonsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LessonsContent(state, caps, viewModel::onEvent)
}

/** 수업·출결: 달 넘기기 → (멘토) 일정 정하기·다른 날 수업 적기 → 멘토마다 이 달 수업. */
@Composable
internal fun LessonsContent(state: LessonsUiState, caps: Capabilities, onEvent: (LessonsEvent) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!caps.has(Feature.LESSONS)) {
            EmptyCard("수업·출결은 튜터 Pro 에서 열려요")
            return@Column
        }
        MonthHeader(state.month, onMove = { onEvent(LessonsEvent.MoveMonth(it)) })
        if (caps.canKeepLessons) MentorTools(state, onEvent)
        if (state.loaded && state.books.isEmpty()) {
            EmptyCard(if (caps.canKeepLessons) "수업 일정을 정하면 수업 날이 여기에 쌓여요" else "선생님이 수업 일정을 정하면 출결이 여기에 보여요")
        }
        val mark = if (caps.canKeepLessons) { d: LocalDate, s: LessonStatus? -> onEvent(LessonsEvent.Mark(d, s)) } else null
        state.books.forEach { LessonBookCard(it, mark) }
    }
}

@Composable
private fun MonthHeader(month: YearMonth, onMove: (Long) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = { onMove(-1) }) { Text("‹ 지난달") }
        Text("${month.year}년 ${month.monthValue}월", style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = { onMove(1) }) { Text("다음 달 ›") }
    }
}

/** 멘토: 수업 일정 정하기 + 정해진 요일이 아닌 날(보강 등)의 수업 적기. */
@Composable
private fun MentorTools(state: LessonsUiState, onEvent: (LessonsEvent) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    var extra by remember { mutableStateOf(DateUtils.today()) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(state.myPlan.label, style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { editing = true }) { Text("일정 정하기") }
            DateField("다른 날 수업", extra, { extra = it; onEvent(LessonsEvent.Mark(it, LessonStatus.DONE)) })
        }
    }
    if (editing) LessonPlanDialog(state.myPlan, onDismiss = { editing = false }, onSave = { onEvent(LessonsEvent.SavePlan(it)) })
}
