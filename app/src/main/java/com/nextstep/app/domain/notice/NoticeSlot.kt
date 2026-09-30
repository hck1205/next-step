package com.nextstep.app.domain.notice

import java.time.LocalDateTime

/** 다음 알림을 보낼 때와 그 종류. */
data class NoticeSlot(val at: LocalDateTime, val kind: NoticeKind)
