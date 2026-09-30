package com.nextstep.app.ui.growthalbum

/** 성장 앨범 화면의 사용자 의도. */
sealed interface GrowthAlbumEvent {
    /** 몇 학년도 앞의 앨범을 볼지(0 = 올해). */
    data class SetYearsBack(val years: Int) : GrowthAlbumEvent
}
