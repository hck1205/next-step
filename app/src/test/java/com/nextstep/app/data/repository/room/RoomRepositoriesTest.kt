package com.nextstep.app.data.repository.room

import com.nextstep.app.data.model.ContentScope
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.data.remote.VideoMetadata
import com.nextstep.app.testing.FakeMetadataFetcher
import com.nextstep.app.domain.planner.StudyPlan
import com.nextstep.app.fake.dao.FakeContentDao
import com.nextstep.app.fake.dao.FakeEventDao
import com.nextstep.app.fake.dao.FakeMemberDao
import com.nextstep.app.fake.dao.FakeNoteDao
import com.nextstep.app.fake.dao.FakeRoadmapDao
import com.nextstep.app.fake.dao.FakeSubjectDao
import com.nextstep.app.fake.dao.FakeTaskDao
import com.nextstep.app.fake.dao.FakeTopicDao
import com.nextstep.app.testing.FakeFamilyScope
import com.nextstep.app.testing.FakeTimeSource
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.testing.RecordingSyncManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/** Room 구현체의 비즈니스 규칙. DAO 는 메모리 Fake 라 Android 없이 돕니다. */
class RoomRepositoriesTest {
    private val scope = FakeFamilyScope()
    private val sync = RecordingSyncManager()
    private val time = FakeTimeSource()

    @Test
    fun subjectSaveFillsFamilyIdStampsTimeAndRequestsPush() = runTest {
        val dao = FakeSubjectDao()
        val repo = RoomSubjectRepository(dao, FakeTopicDao(), scope, sync, time)
        repo.save(Fixtures.subject("s", "수학").copy(familyId = "", dirty = false, updatedAt = 0))
        val saved = dao.getById("s")!!
        assertEquals(Fixtures.FAMILY, saved.familyId); assertTrue(saved.dirty); assertTrue(saved.updatedAt >= 1_000_000L)
        assertEquals(1, sync.pushRequests)
        assertEquals(listOf("수학"), repo.subjects.first().map { it.name })
    }

    @Test
    fun subjectDeleteCascadesSoftDeleteToTopics() = runTest {
        val subjects = FakeSubjectDao().apply { seed(Fixtures.math) }
        val topics = FakeTopicDao().apply { seed(*Fixtures.topics("math", 3, 1).toTypedArray(), Fixtures.topic("eng", "다른", 0)) }
        RoomSubjectRepository(subjects, topics, scope, sync, time).delete("math")
        assertTrue(subjects.getById("math")!!.deleted)
        assertTrue(topics.all.filter { it.subjectId == "math" }.all { it.deleted && it.dirty })
        assertFalse(topics.all.first { it.subjectId == "eng" }.deleted)
        assertTrue(RoomSubjectRepository(subjects, topics, scope, sync, time).subjects.first().isEmpty())
    }

    @Test
    fun topicAddAppendsInOrderAndSkipsBlankTitles() = runTest {
        val dao = FakeTopicDao().apply { seed(Fixtures.topic("math", "기존", 0)) }
        val repo = RoomTopicRepository(dao, scope, sync, time)
        repo.add("math", listOf(" 새1 ", "", "새2"))
        assertEquals(listOf("기존", "새1", "새2"), dao.getBySubject("math").map { it.title })
        assertEquals(listOf(0, 1, 2), dao.getBySubject("math").map { it.orderIndex })
        repo.add("math", listOf("  "))
        assertEquals(3, dao.getBySubject("math").size)
    }

    @Test
    fun classProgressMarksCoveredAndPromotesOnlyUnstartedTopics() = runTest {
        val dao = FakeTopicDao().apply {
            seed(
                Fixtures.topic("math", "a", 0, status = TopicStatus.REVIEWED),
                Fixtures.topic("math", "b", 1, status = TopicStatus.PREVIEWED),
                Fixtures.topic("math", "c", 2),
            )
        }
        val repo = RoomTopicRepository(dao, scope, sync, time)
        repo.setClassProgress("math", 1)
        val byTitle = dao.getBySubject("math").associateBy { it.title }
        assertTrue(byTitle.getValue("a").classCovered); assertEquals(TopicStatus.REVIEWED, byTitle.getValue("a").status)
        assertTrue(byTitle.getValue("b").classCovered); assertEquals(TopicStatus.PREVIEWED, byTitle.getValue("b").status) // 예습 정보 보존
        assertFalse(byTitle.getValue("c").classCovered); assertEquals(TopicStatus.NOT_STARTED, byTitle.getValue("c").status)
        repo.setClassProgress("math", -1)
        assertTrue(dao.getBySubject("math").none { it.classCovered })
    }

    @Test
    fun topicStatusAndDeleteAreSoftAndIgnoreUnknownIds() = runTest {
        val dao = FakeTopicDao().apply { seed(Fixtures.topic("math", "a", 0)) }
        val repo = RoomTopicRepository(dao, scope, sync, time)
        repo.setStatus("t-math-0", TopicStatus.MASTERED)
        assertEquals(TopicStatus.MASTERED, dao.getById("t-math-0")!!.status)
        repo.setStatus("missing", TopicStatus.MASTERED)
        repo.delete("t-math-0")
        assertTrue(dao.getById("t-math-0")!!.deleted)
        assertTrue(repo.observeBySubject("math").first().isEmpty())
        assertEquals(2, sync.pushRequests)
    }

    @Test
    fun taskEventGradeFollowSaveSetDoneDeleteContract() = runTest {
        val tasks = FakeTaskDao(); val taskRepo = RoomTaskRepository(tasks, scope, sync, time)
        taskRepo.save(Fixtures.task("숙제", LocalDate.of(2026, 9, 1)).copy(familyId = ""))
        taskRepo.setDone("task-숙제", true)
        assertTrue(tasks.getById("task-숙제")!!.done)
        taskRepo.delete("task-숙제")
        assertTrue(tasks.getById("task-숙제")!!.deleted)

        val events = FakeEventDao(); val eventRepo = RoomEventRepository(events, scope, sync, time)
        eventRepo.save(Fixtures.event("학원", LocalDate.of(2026, 9, 1), LocalTime.of(9, 0), LocalTime.of(10, 0)))
        eventRepo.delete("ev-학원")
        assertTrue(events.getById("ev-학원")!!.deleted)
        assertTrue(eventRepo.events.first().isEmpty())

        val grades = com.nextstep.app.fake.dao.FakeGradeDao(); val gradeRepo = RoomGradeRepository(grades, scope, sync, time)
        gradeRepo.save(Fixtures.grade("math", 80.0, 1))
        assertEquals(1, gradeRepo.grades.first().size)
        gradeRepo.delete("g-math-1")
        assertTrue(gradeRepo.grades.first().isEmpty())
    }

    @Test
    fun noteAddUsesCurrentProfileAndIgnoresBlank() = runTest {
        val dao = FakeNoteDao()
        val repo = RoomNoteRepository(dao, FakeFamilyScope(Role.PARENT, displayName = "엄마"), sync, time)
        repo.add("   ")
        assertTrue(dao.all.isEmpty())
        repo.add("  힘내  ")
        val note = dao.all.single()
        assertEquals("힘내", note.text); assertEquals("PARENT", note.authorRole); assertEquals("엄마", note.authorName)
        repo.delete(note.id)
        assertTrue(dao.getById(note.id)!!.deleted)
    }

    @Test
    fun roadmapSaveStampsAuthorOnceAndStatusChangesKeepAuthor() = runTest {
        val dao = FakeRoadmapDao()
        val repo = RoomRoadmapRepository(dao, FakeFamilyScope(Role.MENTOR, displayName = "김쌤"), sync, time)
        repo.save(Fixtures.roadmap("개념").copy(familyId = ""))
        val saved = dao.getById("r-개념")!!
        assertEquals("김쌤", saved.createdByName); assertEquals("MENTOR", saved.createdByRole); assertEquals(Fixtures.FAMILY, saved.familyId)
        RoomRoadmapRepository(dao, FakeFamilyScope(Role.STUDENT, displayName = "학생"), sync, time).setStatus("r-개념", RoadmapStatus.DONE)
        assertEquals(RoadmapStatus.DONE, dao.getById("r-개념")!!.status)
        assertEquals("김쌤", dao.getById("r-개념")!!.createdByName)
    }

    @Test
    fun memberRulesMentorAlwaysEnabledAndRemoveIsSoft() = runTest {
        val dao = FakeMemberDao().apply { seed(Fixtures.member(Role.MENTOR, "쌤"), Fixtures.member(Role.PARENT, "엄마")) }
        val repo = RoomMemberRepository(dao, scope, sync, time)
        repo.setMentorEnabled("m-쌤", false)
        assertTrue(dao.getById("m-쌤")!!.mentorEnabled)
        repo.setMentorEnabled("m-엄마", true)
        assertTrue(dao.getById("m-엄마")!!.mentorEnabled)
        repo.setSubjects("m-쌤", listOf("a", "b"))
        assertEquals(listOf("a", "b"), dao.getById("m-쌤")!!.subjectIdList)
        repo.updateProfile("m-쌤", "김쌤", "수학 과외")
        assertEquals("수학 과외", dao.getById("m-쌤")!!.title)
        repo.remove("m-엄마")
        assertEquals(listOf("쌤"), repo.members.first().map { it.name })
        assertNull(dao.getById("nope").also { repo.remove("nope") })
    }

    @Test
    fun myMemberFollowsProfileMemberId() = runTest {
        val dao = FakeMemberDao().apply { seed(Fixtures.member(Role.STUDENT, "나", id = "me")) }
        val repo = RoomMemberRepository(dao, scope, sync, time)
        assertEquals("나", repo.myMember.first()!!.name)
        scope.profileState.value = scope.profileState.value.copy(memberId = null)
        assertNull(repo.myMember.first())
    }

    @Test
    fun studyPlanApplyWritesEventsAndTasksWithFamilyAndSkipsEmpty() = runTest {
        val events = FakeEventDao(); val tasks = FakeTaskDao()
        val repo = RoomStudyPlanRepository(events, tasks, scope, sync, time)
        repo.apply(StudyPlan(emptyList(), emptyList()))
        assertEquals(0, sync.pushRequests)
        repo.apply(StudyPlan(listOf(Fixtures.event("자습", LocalDate.of(2026, 9, 1), LocalTime.of(19, 0), LocalTime.of(20, 0)).copy(familyId = "")), listOf(Fixtures.task("복습", LocalDate.of(2026, 9, 1)).copy(familyId = ""))))
        assertEquals(Fixtures.FAMILY, events.all.single().familyId); assertEquals(Fixtures.FAMILY, tasks.all.single().familyId)
        assertEquals(1, sync.pushRequests)
    }

    @Test
    fun contentPrepareValidatesLinkDedupesAndClassifiesWithFamilySubjects() = runTest {
        val contents = FakeContentDao(); val subjects = FakeSubjectDao().apply { seed(Fixtures.subject("h", "한국사")) }
        val fetcher = FakeMetadataFetcher(VideoMetadata("고1 한국사 조선 후기 강의", "역사쌤", "thumb"))
        val repo = RoomContentRepository(contents, subjects, fetcher, scope, sync, time)
        assertTrue(repo.prepare("https://example.com").isFailure)
        val draft = repo.prepare("https://youtu.be/dQw4w9WgXcQ").getOrThrow()
        assertEquals("dQw4w9WgXcQ", draft.videoId); assertEquals("한국사", draft.classification.subjectKey); assertTrue(draft.metadataFetched)
        assertEquals("thumb", draft.thumbnailUrl)
        repo.save(Fixtures.content("x").copy(videoId = "dQw4w9WgXcQ", familyId = ""))
        assertTrue(repo.prepare("https://youtu.be/dQw4w9WgXcQ").exceptionOrNull()!!.message!!.contains("이미 등록"))
    }

    @Test
    fun contentPrepareFallsBackWhenMetadataMissing() = runTest {
        val repo = RoomContentRepository(FakeContentDao(), FakeSubjectDao(), FakeMetadataFetcher(null), scope, sync, time)
        val draft = repo.prepare("https://youtu.be/dQw4w9WgXcQ").getOrThrow()
        assertFalse(draft.metadataFetched); assertEquals("", draft.title)
        assertTrue(draft.thumbnailUrl.contains("dQw4w9WgXcQ"))
    }

    @Test
    fun contentRatingWatchedAndDeleteRespectScope() = runTest {
        val dao = FakeContentDao().apply {
            seed(Fixtures.content("가족"), Fixtures.content("공용").copy(scope = ContentScope.GLOBAL, familyId = "global"))
        }
        val repo = RoomContentRepository(dao, FakeSubjectDao(), FakeMetadataFetcher(null), scope, sync, time)
        repo.rate("c-가족", 9); repo.rate("c-공용", 5)
        assertEquals(5, dao.getById("c-가족")!!.ratingSum); assertEquals(1, dao.getById("c-가족")!!.ratingCount)
        assertEquals(0, dao.getById("c-공용")!!.ratingCount)
        val pushesBefore = sync.pushRequests
        repo.setWatched("c-공용", true)
        assertTrue(dao.getById("c-공용")!!.watched); assertFalse(dao.getById("c-공용")!!.dirty); assertEquals(pushesBefore, sync.pushRequests)
        repo.setWatched("c-가족", true)
        assertTrue(dao.getById("c-가족")!!.dirty)
        repo.delete("c-공용"); repo.delete("c-가족")
        assertFalse(dao.getById("c-공용")!!.deleted); assertTrue(dao.getById("c-가족")!!.deleted)
        assertEquals(listOf("공용"), repo.contents.first().map { it.title })
    }

    @Test
    fun contentSaveForcesFamilyScopeAndAuthor() = runTest {
        val dao = FakeContentDao()
        val repo = RoomContentRepository(dao, FakeSubjectDao(), FakeMetadataFetcher(null), FakeFamilyScope(Role.MENTOR, displayName = "쌤"), sync, time)
        repo.save(Fixtures.content("x").copy(scope = ContentScope.GLOBAL, familyId = ""))
        val saved = dao.getById("c-x")!!
        assertEquals(ContentScope.FAMILY, saved.scope); assertEquals(Fixtures.FAMILY, saved.familyId); assertEquals("쌤", saved.createdByName)
    }
}
