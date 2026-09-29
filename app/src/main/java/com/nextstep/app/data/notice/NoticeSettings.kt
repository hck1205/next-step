package com.nextstep.app.data.notice

import kotlinx.coroutines.flow.Flow

/** 이 기기에서 알림을 받을지(기기마다 따로, 동기화하지 않음). 처음에는 켜져 있습니다. */
interface NoticeSettings {
    val enabled: Flow<Boolean>
    suspend fun setEnabled(enabled: Boolean)
}
