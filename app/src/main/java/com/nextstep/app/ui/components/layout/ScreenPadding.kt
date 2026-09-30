package com.nextstep.app.ui.components.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

/**
 * 화면 목록(LazyColumn)의 여백. 좌우 16·위 8 은 모두 같고, 아래는 마지막 카드가 + 버튼에 가리지 않을 만큼 둡니다.
 * 화면마다 숫자를 다시 적지 않고 이 둘 중 하나를 씁니다.
 */
object ScreenPadding {
    /** 탭·기록 섹션 화면: + 버튼(56dp)과 그 여백 위로 마지막 카드가 올라오게. */
    val list: PaddingValues = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp)

    /** + 버튼이 없는 자세히 화면. */
    val detail: PaddingValues = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 48.dp)
}
