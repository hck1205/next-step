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
import java.time.LocalDate
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
    fun parentOnboardingCreatesChildAndParentRowsAndCompletesAsParent() = runTest {
        val info = onboarding().createFamilyAsParent("엄마", "아기", LocalDate.of(2026, 7, 1)).getOrThrow()
        assertEquals("아기", info.studentName)
        val child = members.all.single { it.isStudent }; val parent = members.all.single { it.isParent }
        assertEquals(LocalDate.of(2026, 7, 1).toEpochDay(), child.birthDate); assertNull(parent.birthDate)
        val profile = prefs.profile.value
        assertEquals(Role.PARENT, profile.role); assertEquals(parent.id, profile.memberId); assertEquals("아기", profile.studentName); assertTrue(profile.onboarded)
        assertEquals(0, subjects.count(info.familyId))
        assertEquals(info.familyId, sync.startedWith)
        val student = onboarding().createFamilyAsStudent("민수", 3, LocalDate.of(2017, 1, 1)).getOrThrow()
        assertEquals(LocalDate.of(2017, 1, 1).toEpochDay(), members.all.single { it.familyId == student.familyId }.birthDate)
    }

    @Test
    fun parentCanAddSecondChildAndSwitchBetweenChildren() = runTest {
        val first = onboarding().createFamilyAsParent("김수진", "지우", LocalDate.of(2015, 7, 3), relation = "엄마").getOrThrow()
        val me = members.all.single { it.isParent }
        assertEquals("엄마", me.title); assertEquals("엄마", me.roleLabel)
        val second = onboarding().addChildAsParent("하은", LocalDate.of(2020, 5, 1)).getOrThrow()
        var profile = prefs.profile.value
        assertEquals(listOf("지우", "하은"), profile.children.map { it.studentName }); assertTrue(profile.hasSeveralChildren)
        assertEquals(second.familyId, profile.familyId); assertEquals("하은", profile.studentName); assertEquals(second.familyId, sync.startedWith)
        val meInSecond = members.all.single { it.isParent && it.familyId == second.familyId }
        assertEquals("엄마", meInSecond.title); assertEquals("김수진", meInSecond.name); assertEquals(meInSecond.id, profile.memberId)
        onboarding().switchChild(first.familyId)
        profile = prefs.profile.value
        assertEquals(first.familyId, profile.familyId); assertEquals(me.id, profile.memberId); assertEquals("지우", profile.studentName)
        assertEquals(first.familyId, sync.startedWith)
        sync.startedWith = null
        onboarding().switchChild(first.familyId); onboarding().switchChild("unknown")
        assertNull(sync.startedWith)
    }

    @Test
    fun linkChildJoinsAnotherStudentOnceAndOnlyForNonStudents() = runTest {
        val sibling = onboarding().createFamilyAsStudent("민수", 8).getOrThrow()
        prefs.reset()
        onboarding().createFamilyAsParent("아빠", "지우", null, relation = "아빠").getOrThrow()
        val linked = onboarding().linkChild(sibling.pairingCode.lowercase()).getOrThrow()
        assertEquals(sibling.familyId, linked.familyId)
        val profile = prefs.profile.value
        assertEquals(listOf("지우", "민수"), profile.children.map { it.studentName }); assertEquals(sibling.familyId, profile.familyId)
        assertEquals("아빠", members.all.single { it.familyId == sibling.familyId && it.isParent }.title)
        val before = members.all.size
        onboarding().linkChild(sibling.pairingCode).getOrThrow()
        assertEquals(before, members.all.size)
        assertTrue(onboarding().linkChild("NOPE00").isFailure)
        prefs.reset(); onboarding().createFamilyAsStudent("나", 5)
        assertTrue(onboarding().linkChild(sibling.pairingCode).isFailure)
        assertTrue(onboarding().addChildAsParent("x", null).isFailure)
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
