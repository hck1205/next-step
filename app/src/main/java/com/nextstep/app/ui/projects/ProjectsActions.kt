package com.nextstep.app.ui.projects

/** 진행 중 화면 밖으로 나가는 콜백. [onBrowse] 가 null 이면 "새로 시작" 섹션이 보이지 않는 사람(어린 학생)입니다. */
data class ProjectsActions(
    val onOpenProject: (String) -> Unit = { _ -> },
    val onBrowse: (() -> Unit)? = null,
)
