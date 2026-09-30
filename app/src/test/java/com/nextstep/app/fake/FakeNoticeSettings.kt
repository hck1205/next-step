package com.nextstep.app.fake

import com.nextstep.app.data.notice.NoticeSettings
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNoticeSettings(on: Boolean = true) : NoticeSettings {
    override val enabled = MutableStateFlow(on)
    override suspend fun setEnabled(enabled: Boolean) { this.enabled.value = enabled }
}
