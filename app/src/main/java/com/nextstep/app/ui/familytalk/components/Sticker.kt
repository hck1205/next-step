package com.nextstep.app.ui.familytalk.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/** 반짝인 순간 스티커 한 장의 값(그림 · 숫자 · 단위 · 이름 · 색). */
internal data class Sticker(val icon: ImageVector, val value: Int, val unit: String, val label: String, val tint: Color, val container: Color)
