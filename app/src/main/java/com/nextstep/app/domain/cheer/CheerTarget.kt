package com.nextstep.app.domain.cheer

import com.nextstep.app.data.local.entity.TaskEntity

/** 응원할 수 있는 해낸 일 하나와, 내가 이미 붙인 응원([given], 없으면 null). */
data class CheerTarget(val task: TaskEntity, val given: CheerKind?)
