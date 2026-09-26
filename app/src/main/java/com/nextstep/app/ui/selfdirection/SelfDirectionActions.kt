package com.nextstep.app.ui.selfdirection

/** 스스로 화면 밖으로 나가는 콜백. 기록 탭 안에서는 없어도 됩니다. */
data class SelfDirectionActions(
    val onOpenProjects: () -> Unit = {},
)
