package com.nextstep.app.ui.kidme

import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import com.nextstep.app.data.local.entity.RewardEntity
import com.nextstep.app.domain.gamify.Badge
import com.nextstep.app.domain.gamify.GameStyle
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class KidMeViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.STUDENT)
    private val today = LocalDate.of(2029, 10, 10)

    @Test
    fun stickerBoardAndRecentActivitiesNewestFirst() = runTest {
        streams.sessions.value = listOf(Fixtures.session("math", today, LocalTime.of(9, 0), 10))
        streams.activities.value = (1..8).map { Fixtures.activity("활동 $it", date = today.minusDays(it.toLong())) }
        val vm = KidMeViewModel(streams, today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals("학생", s.studentName)
        assertEquals(1 + 8, s.board!!.stickers)
        assertEquals(listOf("활동 1", "활동 2", "활동 3", "활동 4", "활동 5", "활동 6"), s.recentActivities.map { it.title })
        job.cancel()
    }

    @Test
    fun youngChildSeesStickerBoardSpecialStickersAndPromisedPresents() = runTest {
        val kid = Fixtures.member(Role.STUDENT, "하은", id = "kid", gradeYear = 1)
        streams.members.value = listOf(kid)
        val noon = today.atTime(12, 0).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        streams.tasks.value = (1..3).map { Fixtures.task("그림책 $it", today, done = true, by = "PARENT").copy(doneAt = noon) }
        streams.rewards.value = listOf(
            RewardEntity(id = "r1", familyId = Fixtures.FAMILY, kind = "BOARD", targetId = "1", title = "놀이터 30분"),
            RewardEntity(id = "r2", familyId = Fixtures.FAMILY, kind = "BOARD", targetId = "1", title = "쿠키 굽기", givenAt = 1L),
        )
        val vm = KidMeViewModel(streams, today = { today }); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertEquals(GameStyle.STICKERS, s.game!!.style); assertEquals(3, s.game!!.stickersThisWeek)
        assertTrue(s.game!!.badges.any { it.badge == Badge.FIRST_BOARD })
        assertEquals(listOf("놀이터 30분"), s.rewards.map { it.reward.title }) // 받은 선물은 빼고
        assertEquals("스티커판 1장 채우면", s.nextReward!!.condition)
        streams.members.value = listOf(kid.copy(gamify = false)); s = settle(vm.state)
        assertNull(s.game); assertEquals(1, s.rewards.size) // 꺼도 약속한 선물은 그대로
        job.cancel()
    }
}
