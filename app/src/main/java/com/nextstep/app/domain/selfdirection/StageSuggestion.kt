package com.nextstep.app.domain.selfdirection

/** 준비 신호에서 나온 제안: 한 칸 맡기기([up]) 또는 잠깐 같이 하기. 결정은 학부모가 합니다. */
data class StageSuggestion(val to: SelfDirectionStage, val up: Boolean, val reason: String)
