package com.nextstep.app.ui.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel 상태 흐름의 표준 형태. 조합·계산은 기본 디스패처에서 돌리고(메인 스레드 보호),
 * 같은 값은 다시 내보내지 않으며, 구독자가 사라져도 잠시 유지합니다.
 */
fun <T> Flow<T>.asUiState(scope: CoroutineScope, initial: T): StateFlow<T> =
    distinctUntilChanged()
        .flowOn(AppDispatchers.computation)
        .stateIn(scope, SharingStarted.WhileSubscribed(UiDefaults.STATE_STOP_TIMEOUT_MS), initial)
