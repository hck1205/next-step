package com.nextstep.app.domain.familytalk

/** 오늘 화면의 주말 이야기 카드 한 장: 무엇을 보여 줄지와, 기대되는 것이면 해 보고 싶은 것([wish])·가족 즐거움([treat]). */
data class TalkCard(val phase: TalkPhase, val wish: String = "", val treat: String = "")
