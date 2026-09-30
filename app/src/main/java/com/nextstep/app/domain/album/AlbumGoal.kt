package com.nextstep.app.domain.album

import java.time.LocalDate

/** 앨범의 "해낸 것" 한 장: 이룬 목표와 이룬 날. */
data class AlbumGoal(val title: String, val doneOn: LocalDate)
