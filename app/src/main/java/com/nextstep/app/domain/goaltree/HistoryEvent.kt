package com.nextstep.app.domain.goaltree

import java.time.LocalDate

/** 기록 타임라인의 한 줄. [goalTitle] 은 할 일이 속한 목표, [leadsToTitle] 은 달성한 목표가 이어지는 목표. */
data class HistoryEvent(
    val date: LocalDate,
    val kind: HistoryKind,
    val title: String,
    val byRole: String,
    val goalTitle: String? = null,
    val leadsToTitle: String? = null,
)
