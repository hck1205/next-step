package com.nextstep.app.domain.selfdirection

/** 한 바퀴의 한 걸음을 맡는 사람. 어른이 → 같이 → 스스로 순서로 넘겨줍니다. */
enum class Owner(val label: String) {
    ADULT("어른이"),
    TOGETHER("같이"),
    CHILD("스스로"),
}
