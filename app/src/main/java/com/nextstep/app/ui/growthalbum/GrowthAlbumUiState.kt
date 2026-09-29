package com.nextstep.app.ui.growthalbum

import com.nextstep.app.domain.export.ExportDoc

data class GrowthAlbumUiState(
    /** 0 = 올해(이번 학년도), 1 = 작년. */
    val yearsBack: Int = 0,
    val album: ExportDoc? = null,
    val loaded: Boolean = false,
)
