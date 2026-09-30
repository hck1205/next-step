package com.nextstep.app.ui.timer

import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.LocalTime

/** Timer 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface TimerEvent {
    data class SelectSubject(val id: String?) : TimerEvent
    data object Start : TimerEvent
    data object Stop : TimerEvent
    data object Cancel : TimerEvent
    data class AddManual(val subjectId: String?, val date: LocalDate, val start: LocalTime, val minutes: Int, val note: String) : TimerEvent
    data class Delete(val id: String) : TimerEvent
}
