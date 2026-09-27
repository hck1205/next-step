package com.nextstep.app.ui.components.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** 목록이 비었을 때 카드 한 장에 안내 한 줄. */
@Composable
fun EmptyCard(text: String, modifier: Modifier = Modifier) {
    AppCard(modifier) { EmptyState(text) }
}
