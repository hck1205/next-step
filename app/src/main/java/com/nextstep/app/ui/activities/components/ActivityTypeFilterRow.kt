package com.nextstep.app.ui.activities.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.model.ActivityType

/** 전체 · 활동 종류 칩. 고른 칩을 다시 누르면 전체로 돌아갑니다. */
@Composable
internal fun ActivityTypeFilterRow(selected: ActivityType?, onSelect: (ActivityType?) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("전체") })
        ActivityType.entries.forEach { t ->
            FilterChip(selected = selected == t, onClick = { onSelect(if (selected == t) null else t) }, label = { Text(t.label) })
        }
    }
}
