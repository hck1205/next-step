package com.nextstep.app.ui.project

import com.nextstep.app.domain.project.RoutineItem

/** 프로젝트 화면의 사용자 의도. */
sealed interface ProjectEvent {
    /** 오늘 루틴 한 줄 체크(다시 누르면 취소). */
    data class ToggleRoutine(val item: RoutineItem) : ProjectEvent
    /** 지금 단계의 통과 기준을 넘었어요 → 다음 단계로. 마지막 단계면 프로젝트 완료. */
    data object PassCheckpoint : ProjectEvent
    /** 그만두기: 보관해 목록에서 뺍니다(기록은 남음). */
    data object Archive : ProjectEvent
}
