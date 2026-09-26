package com.nextstep.app.ui.projectcatalog

import com.nextstep.app.domain.project.ProjectCategory

/** 새로 시작 화면의 사용자 의도. */
sealed interface ProjectCatalogEvent {
    data class SelectCategory(val category: ProjectCategory?) : ProjectCatalogEvent
    /** [startIndex] 단계부터 오늘 시작합니다(앞 단계는 이미 할 수 있는 것으로 건너뜀). */
    data class Start(val planId: String, val startIndex: Int, val createdByRole: String) : ProjectCatalogEvent
}
