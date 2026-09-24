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
)
