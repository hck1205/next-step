package com.nextstep.app.domain.feedback

import com.nextstep.app.data.model.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedbackVoiceTest {
    private val gap = Finding(FeedbackKind.SUBJECT_GAP, 9, subjectId = "eng", subjectName = "영어")
    private val down = Finding(FeedbackKind.STUDY_DAYS_DOWN, 1, 4)

    @Test
    fun oneFactThreeVoices() {
        val student = FeedbackVoice.line(gap, FeedbackAudience.STUDENT)
        val parent = FeedbackVoice.line(gap, FeedbackAudience.PARENT)
        val mentor = FeedbackVoice.line(gap, FeedbackAudience.MENTOR)
        assertEquals("영어를 한동안 안 봤어요", student.title)
        assertEquals("영어를 9일째 공부하지 않았어요", parent.title)
        assertEquals("영어 9일째 기록 없음", mentor.title)
        assertFalse(student.good); assertEquals(setOf(student.detail, parent.detail, mentor.detail).size, 3) // 할 일은 역할마다 다름
    }

    @Test
    fun kidsHearNoNumbersAndNobodyIsScolded() {
        val kid = FeedbackVoice.line(Finding(FeedbackKind.TASKS_OVERDUE, 4), FeedbackAudience.STUDENT, numbers = false)
        assertFalse(kid.title.any { it.isDigit() })
        assertTrue(FeedbackVoice.line(down, FeedbackAudience.PARENT).detail.contains("물어봐"))
        FeedbackKind.entries.forEach { k ->
            FeedbackAudience.entries.forEach { a -> assertTrue(FeedbackVoice.line(Finding(k, 3, 1, "math", "수학"), a).title.isNotBlank()) }
        }
        assertEquals(FeedbackAudience.PARENT, FeedbackAudience.of(Role.PARENT))
    }
}
