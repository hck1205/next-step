package com.nextstep.app.domain.journey

/** 트랙 표([SubjectTracks]·[LifeTracks])의 단계 한 줄: 어느 학기(나이) · 무엇을 · 어떻게. */
internal object TrackRows {
    fun s(periodKey: String, title: String, detail: String) = TrackStep(periodKey, title, detail)
}
