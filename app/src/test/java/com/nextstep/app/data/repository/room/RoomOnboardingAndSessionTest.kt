package com.nextstep.app.data.repository.room

import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.dao.FakeMemberDao
import com.nextstep.app.fake.dao.FakeStudySessionDao
import com.nextstep.app.fake.dao.FakeSubjectDao
import com.nextstep.app.testing.FakeFamilyScope
import com.nextstep.app.testing.FakePreferences
import com.nextstep.app.testing.FakeTimeSource
import com.nextstep.app.testing.RecordingSyncManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class RoomOnboardingAndSessionTest {
    private val prefs = FakePreferences()
    private val members = FakeMemberDao()
    private val subjects = FakeSubjectDao()
    private val sync = RecordingSyncManager()

    private fun onboarding(sync: RecordingSyncManager = this.sync) = RoomOnboardingRepository(prefs, members, subjects, sync, Random(42))

    @Test
    fun studentOnboardingCreatesFamilyMemberSubjectsAndStartsSync() = runTest {
        val info = onboarding().createFamilyAsStudent("민수", gradeYear = 8).getOrThrow()
        assertEquals(6, info.pairingCode.length)
        assertEquals(8, members.all.single().gradeYear)
        assertTrue(info.pairingCode.none { it in "01IO" })
        val profile = prefs.profile.value
        assertEquals(Role.STUDENT, profile.role); assertTrue(profile.onboarded); assertEquals(info.familyId, profile.familyId)
        val member = members.all.single()
        assertEquals("STUDENT", member.role); assertEquals(member.id, profile.memberId)
        assertEquals(5, subjects.count(info.familyId))
        assertEquals(info.familyId, sync.startedWith)
    }

    @Test
    fun studentOnboardingFailsWhenServerRejects() = runTest {
        val failing = RecordingSyncManager(createResult = Result.failure(IllegalStateException("서버 오류")))
        val result = onboarding(failing).createFamilyAsStudent("민수")
        assertTrue(result.isFailure)
        assertFalse(prefs.profile.value.onboarded); assertTrue(members.all.isEmpty())
    }

    @Test
    fun joinFamilyMatchesCodeCaseInsensitivelyAndRegistersMember() = runTest {
        val info = onboarding().createFamilyAsStudent("민수").getOrThrow()
        val parentPrefs = FakePreferences(); val parentMembers = FakeMemberDao()
        val repo = RoomOnboardingRepository(parentPrefs, parentMembers, FakeSubjectDao(), sync)
        val joined = repo.joinFamily(Role.PARENT, "엄마", " ${info.pairingCode.lowercase()} ").getOrThrow()
        assertEquals(info.familyId, joined.familyId)
        assertEquals(Role.PARENT, parentPrefs.profile.value.role); assertEquals("민수", parentPrefs.profile.value.studentName)
        assertFalse(parentMembers.all.single().mentorEnabled)
        val mentorMembers = FakeMemberDao()
        RoomOnboardingRepository(FakePreferences(), mentorMembers, FakeSubjectDao(), sync).joinFamily(Role.MENTOR, "쌤", info.pairingCode, "수학 과외").getOrThrow()
        assertTrue(mentorMembers.all.single().mentorEnabled); assertEquals("수학 과외", mentorMembers.all.single().title)
    }

    @Test
    fun joinFamilyFailsWithoutSyncOrUnknownCode() = runTest {
        val offline = RoomOnboardingRepository(FakePreferences(), FakeMemberDao(), FakeSubjectDao(), RecordingSyncManager(isAvailable = false))
        assertTrue(offline.joinFamily(Role.PARENT, "엄마", "ABCDEF").isFailure)
        assertTrue(onboarding().joinFamily(Role.PARENT, "엄마", "ZZZZZZ").isFailure)
    }

    @Test
    fun resumeAndSignOutDriveSyncLifecycle() = runTest {
        val repo = onboarding()
        repo.resumeSync(); assertNull(sync.startedWith)
        repo.createFamilyAsStudent("민수").getOrThrow()
        sync.startedWith = null
        repo.resumeSync(); assertNotNull(sync.startedWith)
        repo.signOut()
        assertEquals(1, sync.stopped); assertEquals(1, prefs.resets); assertFalse(repo.profile.first().onboarded)
    }

    @Test
    fun timerStopSavesOnlyWhenAtLeastOneMinuteElapsed() = runTest {
        val dao = FakeStudySessionDao(); val time = FakeTimeSource(start = 0)
        val repo = RoomStudySessionRepository(dao, prefs, FakeFamilyScope(), sync, time)
        repo.startTimer("math")
        assertEquals("math", repo.runningTimer.first()!!.subjectId)
        time.current = 30_000
        assertNull(repo.stopTimer())
        assertTrue(dao.all.isEmpty()); assertNull(repo.runningTimer.first())

        repo.startTimer(null)
        time.current += 25 * 60_000L
        val saved = repo.stopTimer("메모")!!
        assertEquals(25, saved.durationMinutes); assertTrue(saved.fromTimer); assertEquals("메모", saved.note); assertNull(saved.subjectId)
        assertEquals(saved.id, dao.all.single().id)
        assertNull(repo.stopTimer())
    }

    @Test
    fun cancelTimerDiscardsWithoutSaving() = runTest {
        val dao = FakeStudySessionDao()
        val repo = RoomStudySessionRepository(dao, prefs, FakeFamilyScope(), sync, FakeTimeSource())
        repo.startTimer("math"); repo.cancelTimer()
        assertNull(repo.runningTimer.first()); assertTrue(dao.all.isEmpty())
        repo.save(com.nextstep.app.testing.Fixtures.session("math", java.time.LocalDate.of(2026, 9, 1), java.time.LocalTime.of(9, 0), 10).copy(familyId = ""))
        assertEquals(com.nextstep.app.testing.Fixtures.FAMILY, dao.all.single().familyId)
        repo.delete(dao.all.single().id)
        assertTrue(repo.sessions.first().isEmpty())
    }
}
