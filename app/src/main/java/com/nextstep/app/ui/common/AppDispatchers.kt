package com.nextstep.app.ui.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** 화면 상태 계산에 쓰는 디스패처. 테스트는 여기에 테스트 디스패처를 꽂아 가상 시간 안에서 돌립니다. */
object AppDispatchers {
    @Volatile var computation: CoroutineDispatcher = Dispatchers.Default
}
