package com.nextstep.app.ui.settings

import androidx.lifecycle.ViewModel

/** Settings 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface SettingsEvent {
    data object SignOut : SettingsEvent
    data object RequestSync : SettingsEvent
    data class RemoveMember(val id: String) : SettingsEvent
    data class SetMySubjects(val ids: List<String>) : SettingsEvent
    data class SetMentorEnabled(val enabled: Boolean) : SettingsEvent
    data class UpdateMyProfile(val name: String, val title: String) : SettingsEvent
    data class SetGradeYear(val gradeYear: Int) : SettingsEvent
    data class SetBirthDate(val date: java.time.LocalDate?) : SettingsEvent
}
