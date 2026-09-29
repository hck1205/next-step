package com.nextstep.app.ui.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** 오늘 화면에서 관심사 하나를 펼쳤을 때 카드 한 장의 틀: 제목 한 줄과 카드 내용. 카드가 스스로 머리를 가지면 [title] 을 비워 두고, 그러면 줄을 그리지 않습니다. */
@Composable
fun TodayCardFrame(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (title.isNotBlank()) Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        content()
    }
}
