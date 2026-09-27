package com.nextstep.app.domain.stats

import com.nextstep.app.data.model.Role

/** 한 역할이 준 할 일 수와 그중 끝낸 수(누가 준 할 일 차트의 한 칸). */
data class AssignerShare(val role: Role, val given: Int, val done: Int)
