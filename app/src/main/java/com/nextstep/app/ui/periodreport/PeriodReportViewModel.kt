package com.nextstep.app.ui.periodreport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.period.PeriodKind
import com.nextstep.app.domain.period.PeriodReports
import com.nextstep.app.domain.period.Periods
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import com.nextstep.app.ui.common.periodRecords
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

/** 월간·학기 리포트와 학년도 긴 흐름. 이 기간과 앞 기간을 아이 자신의 기록으로만 견줍니다. */
class PeriodReportViewModel(
    streams: FamilyDataStreams,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val kind = MutableStateFlow(PeriodKind.MONTH)
    val state: StateFlow<PeriodReportUiState> = combine(kind, streams.periodRecords(), streams.profile) { k, r, profile ->
        val day = today()
        val now = Periods.current(k, day)
        val before = Periods.previous(now)
        PeriodReportUiState(
            kind = k, report = PeriodReports.report(profile.studentName, now, PeriodReports.stats(now, r), PeriodReports.stats(before, r)),
            months = PeriodReports.monthlySeries(r, day), loaded = true,
        )
    }.asUiState(viewModelScope, PeriodReportUiState())

    fun onEvent(event: PeriodReportEvent) {
        when (event) {
            is PeriodReportEvent.SetKind -> kind.value = event.kind
        }
    }
}
