package com.nextstep.app.domain.album

import java.time.LocalDate

/** 앨범의 "나눈 이야기" 한 장: 주말 이야기에서 자랑한 것 · 해 보고 싶었던 것 · 가족 즐거움. */
data class AlbumTalk(val week: LocalDate, val proud: String, val wish: String, val treat: String)
