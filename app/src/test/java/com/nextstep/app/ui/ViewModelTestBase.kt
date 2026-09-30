package com.nextstep.app.ui

import com.nextstep.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Rule

/** ViewModel 테스트 공통: Main 디스패처 교체와 WhileSubscribed 스트림 구독 헬퍼. */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class ViewModelTestBase {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    /** `stateIn(WhileSubscribed)` 는 구독자가 있어야 흐르므로 테스트 동안 구독을 유지합니다. */
    fun <T> TestScope.subscribe(state: StateFlow<T>): Job = launch { state.collect {} }

    /** 구독 후 모든 코루틴을 진행시키고 현재 값을 돌려줍니다. */
    fun <T> TestScope.settle(state: StateFlow<T>): T { advanceUntilIdle(); return state.value }
}
