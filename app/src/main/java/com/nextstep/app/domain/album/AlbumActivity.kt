package com.nextstep.app.domain.album

import com.nextstep.app.data.model.ActivityType
import java.time.LocalDate

/** 앨범의 "해 본 것" 한 장(폴라로이드): 활동 이름 · 종류 · 날. */
data class AlbumActivity(val title: String, val type: ActivityType, val date: LocalDate)
