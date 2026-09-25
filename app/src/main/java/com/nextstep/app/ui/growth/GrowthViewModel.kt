package com.nextstep.app.ui.growth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.GrowthRepository
import com.nextstep.app.domain.health.GrowthStats
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 성장 › 신체: 키·몸무게·시력 요약, 키 추이, 연도별 전체 기록. */
class GrowthViewModel(
    streams: FamilyDataStreams,
    private val growth: GrowthRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {

    val state: StateFlow<GrowthUiState> = streams.growthRecords.map { all ->
        val day = today()
        val records = all.filter { !it.deleted }.sortedByDescending { it.date }
        GrowthUiState(
            today = day,
            summary = GrowthStats.summarize(records, day),
            byYear = records.groupBy { DateUtils.fromEpochDay(it.date).year }.toList(),
            heightTrend = records.filter { it.heightCm != null }.take(TREND_POINTS).reversed()
                .map { DateUtils.fromEpochDay(it.date).let { d -> "${d.year % CENTURY}.${d.monthValue}" } to it.heightCm!! },
            recordCount = records.size,
            loaded = true,
        )
    }.asUiState(viewModelScope, GrowthUiState())

    fun save(record: GrowthRecordEntity) = viewModelScope.launch { growth.saveRecord(record) }
    fun delete(id: String) = viewModelScope.launch { growth.deleteRecord(id) }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: GrowthEvent) {
        when (event) {
            is GrowthEvent.Save -> save(event.record)
            is GrowthEvent.Delete -> delete(event.id)
        }
    }

    companion object {
        /** 키 추이 차트의 최대 점 개수. */
        const val TREND_POINTS = 12
        private const val CENTURY = 100
    }
}
