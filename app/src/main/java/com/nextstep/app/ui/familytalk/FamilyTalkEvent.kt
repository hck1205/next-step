package com.nextstep.app.ui.familytalk

/** 주말 이야기 화면의 사용자 의도. */
sealed interface FamilyTalkEvent {
    /** 이야기를 남깁니다(자랑 · 해 보고 싶은 것 · 가족 즐거움, 비어 있어도 됨). */
    data class Save(val proud: String, val wish: String, val treat: String) : FamilyTalkEvent
}
