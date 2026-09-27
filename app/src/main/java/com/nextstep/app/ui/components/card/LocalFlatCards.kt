package com.nextstep.app.ui.components.card

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * true 면 [AppCard] 가 자기 바탕·테두리·여백 없이 내용만 그립니다. 슬라이드 타일([com.nextstep.app.ui.components.layout.SlideCard])처럼
 * 이미 카드 모양인 틀 안에서 카드가 겹쳐 보이지 않게 합니다.
 */
val LocalFlatCards = staticCompositionLocalOf { false }
