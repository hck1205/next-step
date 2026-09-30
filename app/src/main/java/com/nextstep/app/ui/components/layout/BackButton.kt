package com.nextstep.app.ui.components.layout

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

/** 상단 바의 뒤로 가기. [onBack] 이 없으면(탭 화면·기록 탭 섹션) 그리지 않습니다. */
@Composable
fun BackButton(onBack: (() -> Unit)?) {
    if (onBack == null) return
    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") }
}
