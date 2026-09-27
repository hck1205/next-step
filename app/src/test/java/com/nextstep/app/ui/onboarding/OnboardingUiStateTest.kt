package com.nextstep.app.ui.onboarding

import com.nextstep.app.data.model.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingUiStateTest {
    @Test
    fun onlyAdultsJoiningAnExistingFamilyNeedACode() {
        assertFalse(OnboardingUiState().joinsWithCode)
        assertFalse(OnboardingUiState(role = Role.STUDENT).joinsWithCode)
        assertTrue(OnboardingUiState(role = Role.MENTOR).joinsWithCode)
        assertTrue(OnboardingUiState(role = Role.PARENT).joinsWithCode)
        val creates = OnboardingUiState(role = Role.PARENT, createAsParent = true)
        assertTrue(creates.parentCreates)
        assertFalse(creates.joinsWithCode)
    }

    @Test
    fun wordingFollowsTheChosenRole() {
        assertEquals("학생 이름", OnboardingUiState().nameLabel)
        assertEquals("시작하기", OnboardingUiState().submitLabel)
        assertEquals("자녀의 여정 시작하기", OnboardingUiState(role = Role.PARENT, createAsParent = true).submitLabel)
        assertEquals("자녀와 연결하기", OnboardingUiState(role = Role.PARENT).submitLabel)
        assertEquals("학생과 연결하기", OnboardingUiState(role = Role.MENTOR).submitLabel)
        assertTrue(OnboardingUiState(role = Role.MENTOR).codeHint.contains("담당 과목"))
    }
}
