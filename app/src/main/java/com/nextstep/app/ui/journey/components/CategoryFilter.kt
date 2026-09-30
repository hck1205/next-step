package com.nextstep.app.ui.journey.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.journey.MilestoneCategory

/** 이정표 종류 칩. [categories] 는 보는 사람이 볼 수 있는 종류만(멘토에게는 건강·재정 없음). */
@Composable
internal fun CategoryFilter(categories: List<MilestoneCategory>, selected: MilestoneCategory?, onSelect: (MilestoneCategory?) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("전체") })
        categories.forEach { c ->
            FilterChip(selected = selected == c, onClick = { onSelect(if (selected == c) null else c) }, label = { Text(c.label) })
        }
    }
}
