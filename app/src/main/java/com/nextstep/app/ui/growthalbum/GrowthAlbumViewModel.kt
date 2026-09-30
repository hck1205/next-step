package com.nextstep.app.ui.growthalbum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.album.GrowthAlbums
import com.nextstep.app.domain.period.PeriodReports
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import com.nextstep.app.ui.common.periodRecords
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

/** 올해의 성장 앨범: 한 학년도를 좋았던 것으로 한 권에(GrowthAlbums). 올해·작년을 고릅니다. */
class GrowthAlbumViewModel(
    streams: FamilyDataStreams,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val yearsBack = MutableStateFlow(0)
    private val family = combine(streams.weekPlans, streams.growthRecords, streams.members) { p, g, m -> Triple(p, g, m) }

    val state: StateFlow<GrowthAlbumUiState> = combine(yearsBack, streams.periodRecords(), family) { back, r, (plans, growth, members) ->
        val name = members.firstOrNull { it.isStudent && !it.deleted }?.name.orEmpty()
        val year = GrowthAlbums.year(today(), back)
        val book = GrowthAlbums.book(name, year, r, plans, growth, today())
        GrowthAlbumUiState(back, book, GrowthAlbums.doc(book, PeriodReports.stats(year, r)), loaded = true)
    }.asUiState(viewModelScope, GrowthAlbumUiState())

    fun onEvent(event: GrowthAlbumEvent) {
        when (event) {
            is GrowthAlbumEvent.SetYearsBack -> yearsBack.value = event.years.coerceIn(0, MAX_YEARS_BACK)
        }
    }

    private companion object {
        const val MAX_YEARS_BACK = 1
    }
}
