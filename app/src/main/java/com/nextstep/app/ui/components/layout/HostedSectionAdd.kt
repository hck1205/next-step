package com.nextstep.app.ui.components.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

/**
 * 섹션의 만들기를 기록 탭 상단 바에 올립니다. [label] 이 null 이면 지금은 만들 수 없음(권한·데이터 없음).
 * 기록 탭 안이면 true 를 돌려주고, 그때 섹션은 자기 + 버튼을 그리지 않습니다(본문·하단에 + 가 겹치지 않게).
 */
@Composable
fun hostedSectionAdd(label: String?, onClick: () -> Unit): Boolean {
    val register = LocalSectionAdd.current ?: return false
    val latest by rememberUpdatedState(onClick)
    DisposableEffect(register, label) {
        register(label?.let { SectionAdd(it) { latest() } })
        onDispose { register(null) }
    }
    return true
}
