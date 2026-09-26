package com.nextstep.app.ui.project

/** 프로젝트 화면 밖으로 나가는 콜백. */
data class ProjectActions(
    val onBack: () -> Unit = {},
)
