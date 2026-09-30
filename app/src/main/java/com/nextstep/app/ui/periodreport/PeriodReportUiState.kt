package com.nextstep.app.ui.periodreport

import com.nextstep.app.domain.export.ExportDoc
import com.nextstep.app.domain.period.MonthPoint
import com.nextstep.app.domain.period.PeriodKind

data class PeriodReportUiState(
    val kind: PeriodKind = PeriodKind.MONTH,
    val report: ExportDoc? = null,
    /** 학년도의 달마다 공부 시간·할 일 끝낸 비율(긴 흐름). */
    val months: List<MonthPoint> = emptyList(),
    val loaded: Boolean = false,
)
