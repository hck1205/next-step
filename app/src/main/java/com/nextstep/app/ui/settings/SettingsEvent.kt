package com.nextstep.app.ui.settings

import androidx.lifecycle.ViewModel
import com.nextstep.app.domain.growth.StudentUiLevel

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
    /** 학생 화면 단계를 직접 고릅니다. null 이면 학년에 맞춰 자동. */
    data class SetStudentLevel(val level: StudentUiLevel?) : SettingsEvent
    /** "학년 고치기" 창에서 한 번에 저장. 바뀐 값만 씁니다. */
    data class SaveStudentYear(val birthDate: java.time.LocalDate?, val gradeYear: Int, val level: StudentUiLevel?) : SettingsEvent
    /** 아이 화면의 레벨·배지(게임 요소) 켜고 끄기. 학부모만. */
    data class SetGamify(val enabled: Boolean) : SettingsEvent
    /** 이 기기의 하루 알림(아침 · 일요일 저녁) 켜고 끄기. */
    data class SetNotices(val enabled: Boolean) : SettingsEvent
    /** 학교: 이름으로 찾기 · 고르기(고르면 바로 학사일정 받기) · 다시 받기. */
    data class SearchSchool(val name: String) : SettingsEvent
    data class PickSchool(val school: com.nextstep.app.domain.school.School) : SettingsEvent
    data object SyncSchool : SettingsEvent
    /** 다자녀 */
    data class SwitchChild(val familyId: String) : SettingsEvent
    data class AddChild(val name: String, val birthDate: java.time.LocalDate?) : SettingsEvent
    data class LinkChild(val code: String) : SettingsEvent
    data object DismissChildError : SettingsEvent
}
