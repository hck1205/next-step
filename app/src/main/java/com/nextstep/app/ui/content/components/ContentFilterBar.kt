package com.nextstep.app.ui.content.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.ui.content.ContentEvent
import com.nextstep.app.ui.content.ContentFilter

/** 검색창과 과목·유형·학년 칩. 같은 칩을 다시 누르면 해제됩니다. */
@Composable
internal fun ContentFilterBar(filter: ContentFilter, subjectKeys: List<String>, showHideWatched: Boolean, onEvent: (ContentEvent) -> Unit) {
    Column {
        OutlinedTextField(value = filter.query, onValueChange = { onEvent(ContentEvent.SetQuery(it)) }, label = { Text("제목·채널·키워드 검색") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(6.dp))
        ChipRow {
            FilterChip(selected = filter.subjectKey == null, onClick = { onEvent(ContentEvent.SetSubject(null)) }, label = { Text("모든 과목") })
            subjectKeys.forEach { k -> FilterChip(selected = filter.subjectKey == k, onClick = { onEvent(ContentEvent.SetSubject(if (filter.subjectKey == k) null else k)) }, label = { Text(k) }) }
        }
        Spacer(Modifier.height(4.dp))
        ChipRow {
            ContentType.entries.forEach { t -> FilterChip(selected = filter.type == t, onClick = { onEvent(ContentEvent.SetType(if (filter.type == t) null else t)) }, label = { Text(t.label) }) }
        }
        Spacer(Modifier.height(4.dp))
        ChipRow {
            LEVELS.forEach { l -> FilterChip(selected = filter.level == l, onClick = { onEvent(ContentEvent.SetLevel(if (filter.level == l) null else l)) }, label = { Text(l.label) }) }
            if (showHideWatched) FilterChip(selected = filter.hideWatched, onClick = { onEvent(ContentEvent.ToggleHideWatched) }, label = { Text("본 영상 숨기기") })
        }
    }
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { content() }
}

private val LEVELS = listOf(GradeLevel.ELEMENTARY, GradeLevel.MIDDLE, GradeLevel.HIGH)
