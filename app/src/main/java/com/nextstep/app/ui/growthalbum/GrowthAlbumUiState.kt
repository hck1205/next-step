package com.nextstep.app.ui.growthalbum

import com.nextstep.app.domain.album.GrowthAlbum
import com.nextstep.app.domain.export.ExportDoc

data class GrowthAlbumUiState(
    /** 0 = 올해(이번 학년도), 1 = 작년. */
    val yearsBack: Int = 0,
    /** 화면이 장마다 그리는 앨범. */
    val book: GrowthAlbum? = null,
    /** 보낼 글·PDF(같은 앨범을 글로). */
    val album: ExportDoc? = null,
    val loaded: Boolean = false,
)
