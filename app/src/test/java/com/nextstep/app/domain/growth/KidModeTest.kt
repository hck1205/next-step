package com.nextstep.app.domain.growth

import com.nextstep.app.data.model.ActivityType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KidModeTest {
    @Test
    fun youngerLevelsGetMoreHelpAndItFadesByGrade() {
        StudentUiLevel.entries.zipWithNext().forEach { (younger, older) -> assertTrue(older.kid.helpCount <= younger.kid.helpCount) }
        assertEquals(KidMode.EARLY, StudentUiLevel.SEED.kid); assertEquals(KidMode.EARLY, StudentUiLevel.SPROUT.kid)
        assertEquals(KidMode.MIDDLE, StudentUiLevel.SEEDLING.kid)
        assertTrue(listOf(StudentUiLevel.STEM, StudentUiLevel.BRANCH, StudentUiLevel.TREE).all { it.kid == KidMode.NONE })
        // 초3~4: 스티커판·읽어 주기는 졸업, 아이용 가족·그림 기록·보이는 타이머는 유지
        assertTrue(!KidMode.MIDDLE.stickerMe && !KidMode.MIDDLE.readsAloud && KidMode.MIDDLE.kidFamily && KidMode.MIDDLE.pictureRecord && KidMode.MIDDLE.visualTimer)
        assertEquals(5, KidMode.EARLY.helpCount); assertEquals(0, KidMode.NONE.helpCount)
    }

    @Test
    fun kidRecordsAreOneTapActivitiesWithReadableTitles() {
        assertEquals(6, KidRecord.entries.size)
        KidRecord.entries.forEach { assertTrue(it.label.endsWith("요")); assertTrue(it.title.isNotBlank()) }
        assertEquals(ActivityType.VOLUNTEER, KidRecord.HELP.type)
        assertTrue(KidRecord.MUSIC.title.contains("피아노")) // 소질 신호가 음악으로 알아보도록
    }
}
