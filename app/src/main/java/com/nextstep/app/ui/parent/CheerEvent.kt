package com.nextstep.app.ui.parent

import androidx.lifecycle.ViewModel

/** Cheer 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface CheerEvent {
    data class Send(val text: String) : CheerEvent
    data class Delete(val id: String) : CheerEvent
}
