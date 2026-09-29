package com.nextstep.app.ui.periodreport

import com.nextstep.app.domain.period.PeriodKind

/** 월간·학기 리포트 화면의 사용자 의도. */
sealed interface PeriodReportEvent {
    data class SetKind(val kind: PeriodKind) : PeriodReportEvent
}
