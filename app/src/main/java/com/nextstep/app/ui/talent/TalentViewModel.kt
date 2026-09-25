package com.nextstep.app.ui.talent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.ObservationEntity
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.GrowthRepository
import com.nextstep.app.domain.insight.AptitudeEngine
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 활동·재능 › 재능: 소질 신호(활동 + 관찰)와 영역별 관찰 메모 전체. */
class TalentViewModel(
    streams: FamilyDataStreams,
    private val growth: GrowthRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {

    val state: StateFlow<TalentUiState> = combine(streams.activities, streams.observations) { activities, all ->
        val day = today()
        val observations = all.filter { !it.deleted }.sortedByDescending { it.date }
        TalentUiState(
            today = day,
            signals = AptitudeEngine.signals(activities, observations, day),
            byDomain = observations.groupBy { it.domain }.toList().sortedByDescending { it.second.size },
            observationCount = observations.size,
            loaded = true,
        )
    }.asUiState(viewModelScope, TalentUiState())

    fun observe(observation: ObservationEntity) = viewModelScope.launch { growth.addObservation(observation) }
    fun delete(id: String) = viewModelScope.launch { growth.deleteObservation(id) }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: TalentEvent) {
        when (event) {
            is TalentEvent.Observe -> observe(event.observation)
            is TalentEvent.Delete -> delete(event.id)
        }
    }
}
