package com.nextstep.app.ui.kidfamily

import com.nextstep.app.domain.cheer.KidMessage

/** 아이용 가족 탭의 사용자 의도. */
sealed interface KidFamilyEvent {
    data class Send(val message: KidMessage) : KidFamilyEvent
    data object OpenSettings : KidFamilyEvent
    data class AnswerGate(val answer: String) : KidFamilyEvent
    data object CloseGate : KidFamilyEvent
    data object ConsumeUnlock : KidFamilyEvent
    data object ClearSent : KidFamilyEvent
}
