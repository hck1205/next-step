package com.nextstep.app.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.data.local.entity.ObservationEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.GrowthRepository
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.health.GrowthStats
import com.nextstep.app.domain.insight.AptitudeEngine
import com.nextstep.app.domain.journey.PeriodCalendar
import com.nextstep.app.domain.stats.BalanceStats
import com.nextstep.app.domain.time.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 기록 탭의 균형 세그먼트: 학습 균형, 성장 기록(키·몸무게·시력), 소질 신호를 한 상태로 냅니다.
 * 다른 세그먼트는 각 기능 화면이 자기 ViewModel 로 그립니다.
 */
class RecordsViewModel(
    private val streams: FamilyDataStreams,
    private val growth: GrowthRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val base = combine(streams.profile, streams.members, streams.sessions, streams.tasks, streams.activities) { profile, members, sessions, tasks, activities ->
        val day = today()
        val stage = GrowthStage.of(members, day)
        val birthDate = members.firstOrNull { it.role == Role.STUDENT.name }?.birthDate?.let { LocalDate.ofEpochDay(it) }
        val period = birthDate?.let { PeriodCalendar.current(it, day) }
        RecordsUiState(
            studentName = profile.studentName,
            stage = stage,
            currentPeriodLabel = period?.label,
            today = day,
            balance = BalanceStats.report(stage, sessions, tasks, activities, period, day),
            loaded = true,
        ) to activities
    }

    val state: StateFlow<RecordsUiState> = combine(base, streams.growthRecords, streams.observations) { (s, activities), records, observations ->
        s.copy(
            growth = GrowthStats.summarize(records, s.today),
            growthRecords = records.filter { !it.deleted }.sortedByDescending { it.date }.take(MAX_RECORDS),
            aptitude = AptitudeEngine.signals(activities, observations, s.today),
            observations = observations.filter { !it.deleted }.sortedByDescending { it.date }.take(MAX_RECORDS),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordsUiState())

    fun saveGrowth(record: GrowthRecordEntity) = viewModelScope.launch { growth.saveRecord(record) }
    fun deleteGrowth(id: String) = viewModelScope.launch { growth.deleteRecord(id) }
    fun addObservation(observation: ObservationEntity) = viewModelScope.launch { growth.addObservation(observation) }
    fun deleteObservation(id: String) = viewModelScope.launch { growth.deleteObservation(id) }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: RecordsEvent) {
        when (event) {
            is RecordsEvent.SaveGrowth -> saveGrowth(event.record)
            is RecordsEvent.DeleteGrowth -> deleteGrowth(event.id)
            is RecordsEvent.AddObservation -> addObservation(event.observation)
            is RecordsEvent.DeleteObservation -> deleteObservation(event.id)
        }
    }

    private companion object {
        const val MAX_RECORDS = 5
    }
}
