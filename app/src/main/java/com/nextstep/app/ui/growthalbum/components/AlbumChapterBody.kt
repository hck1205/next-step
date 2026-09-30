package com.nextstep.app.ui.growthalbum.components

import androidx.compose.runtime.Composable
import com.nextstep.app.domain.album.GrowthAlbum
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.chart.YearHeatStrip
import com.nextstep.app.ui.growthalbum.AlbumChapter

/** 장마다의 한 줄 요약(좋았던 것의 크기만, 비교 없음). */
internal fun albumSummary(chapter: AlbumChapter, book: GrowthAlbum): String = when (chapter) {
    AlbumChapter.GOALS -> "목표 ${book.goals.size}개를 이뤘어요"
    AlbumChapter.STEADY -> "공부한 날 ${book.studyDays}일 · 가장 길게 이어서 ${book.longestStreak}일"
    AlbumChapter.TRIED -> "새로운 경험 ${book.activities.size}번"
    AlbumChapter.GREW -> "키는 천천히, 꾸준히 자라요"
    AlbumChapter.CHEERS -> "응원 ${book.cheers.size}개를 받았어요"
    AlbumChapter.TALKS -> "주말 이야기 ${book.talks.size}번"
}

/** 장의 몸: 메달 · 한 해 공부 달력 · 폴라로이드 · 키 자 · 응원 말풍선 · 이야기 인용. */
@Composable
internal fun AlbumChapterBody(chapter: AlbumChapter, book: GrowthAlbum) {
    when (chapter) {
        AlbumChapter.GOALS -> book.goals.forEach { MedalCard(it) }
        AlbumChapter.STEADY -> AppCard { YearHeatStrip(book.studyWeeks) }
        AlbumChapter.TRIED -> PolaroidGrid(book.activities)
        AlbumChapter.GREW -> book.height?.let { HeightRuler(it) }
        AlbumChapter.CHEERS -> CheerBubbles(book.cheers)
        AlbumChapter.TALKS -> TalkQuotes(book.talks)
    }
}
