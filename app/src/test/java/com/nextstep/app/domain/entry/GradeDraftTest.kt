package com.nextstep.app.domain.entry

import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.model.ExamType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GradeDraftTest {
    private val date = LocalDate.of(2026, 9, 1)
    private val draft = GradeDraft("math", " 1학기 중간 ", ExamType.MIDTERM, 88.0, 100.0, 70.0, date, " 분수 실수 ")

    @Test
    fun newGradeTrimsTextAndStoresEpochDay() {
        val g = draft.toEntity()
        assertEquals("1학기 중간", g.title); assertEquals("분수 실수", g.memo)
        assertEquals(date.toEpochDay(), g.date); assertEquals(ExamType.MIDTERM, g.examType)
        assertEquals(70.0, g.classAverage!!, 0.0); assertEquals("", g.familyId)
    }

    @Test
    fun editKeepsIdAndFamilyAndClearsAverage() {
        val old = GradeEntity(familyId = "fam", subjectId = "eng", title = "옛 시험", score = 50.0, date = 1, classAverage = 60.0)
        val g = draft.copy(classAverage = null).toEntity(old)
        assertEquals(old.id, g.id); assertEquals("fam", g.familyId); assertEquals("math", g.subjectId)
        assertEquals(88.0, g.score, 0.0); assertNull(g.classAverage)
    }
}
