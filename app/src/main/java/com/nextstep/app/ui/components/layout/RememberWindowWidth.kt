package com.nextstep.app.ui.components.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/** 지금 창의 폭 단계. 회전·접기·멀티 윈도우로 폭이 바뀌면 다시 계산됩니다. */
@Composable
fun rememberWindowWidth(): WindowWidth = WindowWidth.of(LocalConfiguration.current.screenWidthDp)
