package com.nextstep.app.domain.stats

enum class BalanceVerdict(val label: String) {
    NONE("해당 없음"),
    MORE("조금 더"),
    WITHIN("권장선 안"),
    LESS("줄이기"),
}
