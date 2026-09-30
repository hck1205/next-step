package com.nextstep.app.ui.kidfamily

/** 아이용 가족 탭 밖으로: 어른 확인을 통과하면 설정 화면으로. */
data class KidFamilyActions(
    val onOpenSettings: () -> Unit = {},
)
