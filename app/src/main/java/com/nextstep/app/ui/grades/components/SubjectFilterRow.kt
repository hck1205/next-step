package com.nextstep.app.ui.grades.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity

/** 전체 · 과목별 거르기 칩(옆으로 밀림). */
@Composable
internal fun SubjectFilterRow(subjects: List<SubjectEntity>, selectedId: String?, onSelect: (String?) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FilterChip(selected = selectedId == null, onClick = { onSelect(null) }, label = { Text("전체") })
        subjects.forEach { s -> FilterChip(selected = selectedId == s.id, onClick = { onSelect(s.id) }, label = { Text(s.name) }) }
    }
}
