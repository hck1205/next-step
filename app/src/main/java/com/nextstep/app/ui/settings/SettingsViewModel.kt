package com.nextstep.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.notice.NoticeSettings
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.MemberRepository
import com.nextstep.app.data.repository.OnboardingRepository
import com.nextstep.app.data.school.SchoolService
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.domain.growth.YearProfiles
import com.nextstep.app.domain.school.School
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val streams: FamilyDataStreams,
    private val onboarding: OnboardingRepository,
    private val members: MemberRepository,
    private val notices: NoticeSettings,
    private val school: SchoolService,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val childError = MutableStateFlow<String?>(null)
    private val schoolSearch = MutableStateFlow(SchoolSearchState())

    val state: StateFlow<SettingsUiState> = combine(streams.profile, streams.syncStatus, streams.members, streams.myMember, streams.subjects) { p, s, members, me, subjects ->
        val student = members.firstOrNull { it.isStudent }
        val birth = student?.birthDate?.let { DateUtils.fromEpochDay(it) }
        SettingsUiState(
            p, s, onboarding.syncAvailable, members, me, subjects, student, birth, birth?.let { GrowthStage.ageLabel(it, DateUtils.today()) },
            autoStudentLevel = student?.let { StudentUiLevel.auto(it) },
            chosenStudentLevel = StudentUiLevel.fromName(student?.uiLevel),
            yearLabel = student?.let { YearProfiles.of(it)?.label },
        )
    }.combine(childError) { s, e -> s.copy(childError = e) }
        .combine(notices.enabled) { s, on -> s.copy(noticesOn = on) }
        .combine(schoolSearch) { s, search -> s.copy(schoolAvailable = school.available, schoolSearch = search) }
        .asUiState(viewModelScope, SettingsUiState())

    fun switchChild(familyId: String) { viewModelScope.launch { onboarding.switchChild(familyId) } }
    fun addChild(name: String, birthDate: LocalDate?) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            onboarding.addChildAsParent(name.trim(), birthDate).onFailure { childError.value = it.message }
        }
    }
    fun linkChild(code: String) {
        viewModelScope.launch {
            if (code.isBlank()) return@launch
            onboarding.linkChild(code).onFailure { childError.value = it.message }
        }
    }

    private fun searchSchool(name: String) {
        viewModelScope.launch {
            schoolSearch.value = schoolSearch.value.copy(busy = true, message = null)
            school.search(name).fold(
                onSuccess = { schoolSearch.value = SchoolSearchState(it, if (it.isEmpty()) "찾는 학교가 없어요. 이름을 다시 확인해 주세요." else null) },
                onFailure = { schoolSearch.value = SchoolSearchState(message = "학교를 찾지 못했어요. 잠시 뒤 다시 해 주세요.") },
            )
        }
    }

    /** 학생 구성원에 학교를 남기고 바로 학사일정을 받아 옵니다. */
    private fun pickSchool(picked: School) {
        viewModelScope.launch {
            val student = state.value.student ?: return@launch
            members.setSchool(student.id, picked.key, picked.name)
            syncSchool()
        }
    }

    private suspend fun syncSchool() {
        schoolSearch.value = SchoolSearchState(busy = true)
        val message = school.syncNow(today()).fold(
            onSuccess = { if (it == 0) "새로 들어온 학교 일정이 없어요" else "학교 일정 ${it}개를 가족 달력에 넣었어요" },
            onFailure = { "학교 일정을 받지 못했어요. 잠시 뒤 다시 받아 주세요." },
        )
        schoolSearch.value = SchoolSearchState(message = message)
    }

    fun signOut() { viewModelScope.launch { onboarding.signOut() } }
    fun requestSync() = onboarding.requestSync()
    fun removeMember(id: String) { viewModelScope.launch { members.remove(id) } }
    fun setMySubjects(ids: List<String>) { viewModelScope.launch { state.value.me?.let { members.setSubjects(it.id, ids) } } }
    fun setMentorEnabled(enabled: Boolean) { viewModelScope.launch { state.value.me?.let { members.setMentorEnabled(it.id, enabled) } } }
    fun setGradeYear(gradeYear: Int) { viewModelScope.launch { state.value.members.firstOrNull { it.isStudent }?.let { members.setGradeYear(it.id, gradeYear) } } }
    fun setBirthDate(date: LocalDate?) { viewModelScope.launch { state.value.members.firstOrNull { it.isStudent }?.let { members.setBirthDate(it.id, date) } } }
    fun setStudentLevel(level: StudentUiLevel?) { viewModelScope.launch { state.value.student?.let { members.setUiLevel(it.id, level) } } }
    fun saveStudentYear(birthDate: LocalDate?, gradeYear: Int, level: StudentUiLevel?) {
        viewModelScope.launch {
            val student = state.value.student ?: return@launch
            if (birthDate != state.value.birthDate) members.setBirthDate(student.id, birthDate)
            if (gradeYear != student.gradeYear) members.setGradeYear(student.id, gradeYear)
            if (level != state.value.chosenStudentLevel) members.setUiLevel(student.id, level)
        }
    }
    fun setGamify(enabled: Boolean) { viewModelScope.launch { state.value.student?.let { members.setGamify(it.id, enabled) } } }
    fun updateMyProfile(name: String, title: String) { viewModelScope.launch { state.value.me?.let { members.updateProfile(it.id, name, title) } } }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.SignOut -> signOut()
            SettingsEvent.RequestSync -> requestSync()
            is SettingsEvent.RemoveMember -> removeMember(event.id)
            is SettingsEvent.SetMySubjects -> setMySubjects(event.ids)
            is SettingsEvent.SetMentorEnabled -> setMentorEnabled(event.enabled)
            is SettingsEvent.UpdateMyProfile -> updateMyProfile(event.name, event.title)
            is SettingsEvent.SetGradeYear -> setGradeYear(event.gradeYear)
            is SettingsEvent.SetStudentLevel -> setStudentLevel(event.level)
            is SettingsEvent.SetBirthDate -> setBirthDate(event.date)
            is SettingsEvent.SaveStudentYear -> saveStudentYear(event.birthDate, event.gradeYear, event.level)
            is SettingsEvent.SetGamify -> setGamify(event.enabled)
            is SettingsEvent.SetNotices -> viewModelScope.launch { notices.setEnabled(event.enabled) }
            is SettingsEvent.SearchSchool -> searchSchool(event.name)
            is SettingsEvent.PickSchool -> pickSchool(event.school)
            SettingsEvent.SyncSchool -> viewModelScope.launch { syncSchool() }
            is SettingsEvent.SwitchChild -> switchChild(event.familyId)
            is SettingsEvent.AddChild -> addChild(event.name, event.birthDate)
            is SettingsEvent.LinkChild -> linkChild(event.code)
            SettingsEvent.DismissChildError -> childError.value = null
        }
    }

}
