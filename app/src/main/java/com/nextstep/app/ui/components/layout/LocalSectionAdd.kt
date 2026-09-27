package com.nextstep.app.ui.components.layout

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 기록 탭이 섹션에게 주는 자리: 섹션이 여기에 자기 [SectionAdd] 를 올리면(없으면 null) 기록 탭 상단 바에 보입니다.
 * 기록 탭 밖(따로 연 화면)에서는 null 이라 섹션이 스스로 + 버튼을 그립니다.
 */
val LocalSectionAdd = staticCompositionLocalOf<((SectionAdd?) -> Unit)?> { null }
