package com.nextstep.app.ui.familycalendar.components

import androidx.compose.runtime.Composable
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.ui.components.input.ChipRow

/** 누구의 일정을 볼지: 모두 · 구성원마다 한 칩(한 사람을 고르면 그 사람 일정과 가족 모두의 일정). */
@Composable
internal fun WhoFilterRow(members: List<MemberEntity>, selected: String?, onSelect: (String?) -> Unit) {
    val options = listOf<MemberEntity?>(null) + members
    ChipRow(options, selected = { it?.id == selected }, label = { it?.name ?: "모두" }, onClick = { onSelect(it?.id) })
}
