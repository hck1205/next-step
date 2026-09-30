package com.nextstep.app.domain.album

import com.nextstep.app.domain.cheer.CheerKind
import java.time.LocalDate

/** 앨범의 "받은 응원" 말풍선 하나: 누가 · 어떤 일에 · 어떤 응원. */
data class AlbumCheer(val fromName: String, val taskTitle: String, val kind: CheerKind, val on: LocalDate)
