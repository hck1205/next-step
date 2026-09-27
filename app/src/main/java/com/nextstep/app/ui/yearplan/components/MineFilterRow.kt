package com.nextstep.app.ui.yearplan.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/** 내 할 일만 / 가족 전체. 둘이 다를 때만 보입니다. */
@Composable
internal fun MineFilterRow(mineOnly: Boolean, mineCount: Int, allCount: Int, onShowMine: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = mineOnly, onClick = { onShowMine(true) }, label = { Text("내 할 일 $mineCount") })
        FilterChip(selected = !mineOnly, onClick = { onShowMine(false) }, label = { Text("가족 전체 $allCount") })
    }
}
