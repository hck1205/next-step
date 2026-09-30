package com.nextstep.app.domain.album

import com.nextstep.app.domain.period.Period
import com.nextstep.app.domain.stats.HeatWeek

/**
 * 한 학년도의 성장 앨범 한 권(화면이 장마다 그리는 값). 좋았던 것만 담습니다 — 점수·비교·밀린 것은 없습니다.
 * [studyWeeks] 는 학년도 첫 주부터 이번 주(지난 학년도면 마지막 주)까지의 공부 달력.
 */
data class GrowthAlbum(
    val studentName: String,
    val year: Period,
    val goals: List<AlbumGoal> = emptyList(),
    val studyDays: Int = 0,
    val longestStreak: Int = 0,
    val studyWeeks: List<HeatWeek> = emptyList(),
    val activities: List<AlbumActivity> = emptyList(),
    val height: HeightChange? = null,
    val cheers: List<AlbumCheer> = emptyList(),
    val talks: List<AlbumTalk> = emptyList(),
) {
    val isEmpty: Boolean get() = goals.isEmpty() && studyDays == 0 && activities.isEmpty() && height == null && cheers.isEmpty() && talks.isEmpty()
}
