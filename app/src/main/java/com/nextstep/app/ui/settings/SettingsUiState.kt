package com.nextstep.app.ui.settings

import com.nextstep.app.domain.gamify.GameStyle
import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.data.prefs.LinkedChild
import java.time.LocalDate
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.data.prefs.UserProfile

data class SettingsUiState(
    val profile: UserProfile? = null,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val syncAvailable: Boolean = false,
    /** 이 학생에 연결된 모든 구성원 (학생 본인, 학부모들, 멘토들). */
    val members: List<MemberEntity> = emptyList(),
    val me: MemberEntity? = null,
    val subjects: List<SubjectEntity> = emptyList(),
    /** 학생 구성원과 생년월일·나이 표기. 생년월일이 없으면 null. */
    val student: MemberEntity? = null,
    val birthDate: LocalDate? = null,
    val ageLabel: String? = null,
    /** 연결 실패 같은 자녀 관련 안내. */
    val childError: String? = null,
    /** 학생 화면 단계: 학년으로 정한 자동 값과 학부모가 고른 값(없으면 자동). */
    val autoStudentLevel: StudentUiLevel? = null,
    val chosenStudentLevel: StudentUiLevel? = null,
    /** 올해 학년 표기(예: "초5", "만 4세"). 생년월일·학년이 없으면 null. */
    val yearLabel: String? = null,
) {
    val children: List<LinkedChild> get() = profile?.children.orEmpty()
    /** 아이 나이에 맞춘 게임 모양(학부모가 고른 화면 단계가 있으면 그 단계). 성장 기록 모양이면 학생도 스스로 끌 수 있습니다. */
    val gameStyle: GameStyle get() = (chosenStudentLevel ?: autoStudentLevel ?: StudentUiLevel.TREE).game
    val activeFamilyId: String? get() = profile?.familyId
}
