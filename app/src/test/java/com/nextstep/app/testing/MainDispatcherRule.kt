package com.nextstep.app.testing

import com.nextstep.app.ui.common.AppDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** viewModelScope 의 Main 과 상태 계산 디스패처를 같은 테스트 디스패처로 바꿔 가상 시간 안에서 돌립니다. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(val dispatcher: TestDispatcher = StandardTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
        AppDispatchers.computation = dispatcher
    }

    override fun finished(description: Description) {
        AppDispatchers.computation = Dispatchers.Default
        Dispatchers.resetMain()
    }
}
