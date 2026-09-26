package com.nextstep.app.data.repository.room

import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.dao.FakeRewardDao
import com.nextstep.app.testing.FakeFamilyScope
import com.nextstep.app.testing.FakeTimeSource
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.testing.RecordingSyncManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomRewardRepositoryTest {
    private val dao = FakeRewardDao()
    private val sync = RecordingSyncManager()
    private val repo = RoomRewardRepository(dao, FakeFamilyScope(Role.PARENT), sync, FakeTimeSource(7L))

    @Test
    fun promiseReplacesAnOpenOneThenGiveAndCancel() = runTest {
        repo.promise("GOAL", "g1", "  ")
        repo.promise("GOAL", "", "보드게임")
        assertTrue(dao.all.isEmpty())
        repo.promise("GOAL", "g1", " 보드게임 ")
        repo.promise("GOAL", "g1", "영화 보기")
        val r = dao.all.single()
        assertEquals("영화 보기", r.title); assertEquals("PARENT", r.createdByRole); assertEquals(Fixtures.FAMILY, r.familyId)
        repo.give(r.id)
        val given = dao.getById(r.id)!!
        assertNotNull(given.givenAt); assertEquals("PARENT", given.givenByRole)
        repo.give(r.id)
        assertEquals(given.givenAt, dao.getById(r.id)!!.givenAt)
        repo.promise("GOAL", "g1", "다음 보상") // 이미 준 약속은 두고 새로
        assertEquals(2, dao.all.size)
        repo.cancel(dao.all.first { it.givenAt == null }.id)
        assertEquals(listOf("영화 보기"), repo.rewards.first().map { it.title })
        assertTrue(sync.pushRequests >= 4)
    }
}
