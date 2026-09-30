package com.nextstep.app.ui.journey.components

import com.nextstep.app.domain.journey.JourneyItem

/** 여정 화면에서 한 번에 하나만 열리는 창. */
internal sealed interface JourneyDialog {
    data object Add : JourneyDialog
    data class Note(val item: JourneyItem) : JourneyDialog
    data class DueDate(val item: JourneyItem) : JourneyDialog
}
