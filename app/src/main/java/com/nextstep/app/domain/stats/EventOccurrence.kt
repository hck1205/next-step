package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.EventEntity

data class EventOccurrence(val event: EventEntity, val startAt: Long, val endAt: Long)
