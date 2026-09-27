package com.nextstep.app.ui.components.layout

import androidx.compose.ui.graphics.vector.ImageVector

/** 머리 ⋮ 메뉴의 한 줄: 다른 화면으로 가는 바로가기. */
data class AppBarMenuItem(val label: String, val icon: ImageVector, val onClick: () -> Unit)
