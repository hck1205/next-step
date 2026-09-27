package com.nextstep.app.ui.onboarding

import com.nextstep.app.data.model.GuardianRelation
import com.nextstep.app.data.model.Role

data class OnboardingUiState(
    val step: Int = 0,
    val role: Role? = null,
    val name: String = "",
    val code: String = "",
    /** 멘토 구분 (예: 수학 과외). */
    val title: String = "",
    /** 학부모의 관계(엄마·아빠·할머니·할아버지·보호자). 한 자녀에 여러 보호자가 연결됩니다. */
    val relation: GuardianRelation? = null,
    /** 학생 학년(1~18). 0 이면 미선택. */
    val gradeYear: Int = 0,
    /** 학생(자녀) 생년월일. 여정 타임라인의 기준. */
    val birthDate: java.time.LocalDate? = null,
    /** 학부모가 자녀 기기 없이 직접 가족을 만드는 경우 (영유아 등). */
    val createAsParent: Boolean = false,
    val childName: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val syncAvailable: Boolean = false,
) {
    /** 화면에 보이는 역할. 아직 고르기 전이면 학생 화면으로 보여 줍니다. */
    val shownRole: Role get() = role ?: Role.STUDENT
    /** 학부모가 자녀 기기 없이 직접 가족을 만드는지. */
    val parentCreates: Boolean get() = role == Role.PARENT && createAsParent
    /** 이미 있는 가족에 연결 코드로 들어가는지: 학생이 아니고, 학부모가 직접 만들지도 않을 때. */
    val joinsWithCode: Boolean get() = role != null && role != Role.STUDENT && !parentCreates

    val nameLabel: String get() = when (shownRole) {
        Role.STUDENT -> "학생 이름"
        Role.PARENT -> "이름"
        Role.MENTOR -> "이름 (예: 김선생)"
    }

    val codeHint: String get() =
        if (shownRole == Role.MENTOR) "학생(또는 학부모) 앱의 설정 화면에 표시된 연결 코드를 입력하세요. 연결 후 담당 과목을 고를 수 있어요."
        else "자녀의 앱 → 설정 화면에 표시된 연결 코드를 입력하세요."

    val submitLabel: String get() = when {
        shownRole == Role.STUDENT -> "시작하기"
        parentCreates -> "자녀의 여정 시작하기"
        shownRole == Role.PARENT -> "자녀와 연결하기"
        else -> "학생과 연결하기"
    }
}
