package com.nextstep.app.ui.projectcatalog

/** 새로 시작 화면 밖으로 나가는 콜백. 시작한 뒤에는 진행 중 섹션으로 돌아갑니다. */
data class ProjectCatalogActions(
    val onStarted: () -> Unit = {},
)
