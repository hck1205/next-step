package com.nextstep.app.di

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nextstep.app.data.local.AppDatabase
import com.nextstep.app.data.prefs.UserPreferences
import com.nextstep.app.data.remote.YouTubeMetadataFetcher
import com.nextstep.app.data.repository.ContentRepository
import com.nextstep.app.data.repository.EventRepository
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.GoalRepository
import com.nextstep.app.data.repository.GradeRepository
import com.nextstep.app.data.repository.JourneyRepository
import com.nextstep.app.data.repository.MemberRepository
import com.nextstep.app.data.repository.NoteRepository
import com.nextstep.app.data.repository.OnboardingRepository
import com.nextstep.app.data.repository.RoadmapRepository
import com.nextstep.app.data.repository.StudyPlanRepository
import com.nextstep.app.data.repository.StudySessionRepository
import com.nextstep.app.data.repository.SubjectRepository
import com.nextstep.app.data.repository.SystemTimeSource
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.TopicRepository
import com.nextstep.app.data.repository.room.CompositeFamilyDataStreams
import com.nextstep.app.data.repository.room.PrefsFamilyScope
import com.nextstep.app.data.repository.room.RoomContentRepository
import com.nextstep.app.data.repository.room.RoomEventRepository
import com.nextstep.app.data.repository.room.RoomGoalRepository
import com.nextstep.app.data.repository.room.RoomGradeRepository
import com.nextstep.app.data.repository.room.RoomJourneyRepository
import com.nextstep.app.data.repository.room.RoomMemberRepository
import com.nextstep.app.data.repository.room.RoomNoteRepository
import com.nextstep.app.data.repository.room.RoomOnboardingRepository
import com.nextstep.app.data.repository.room.RoomRoadmapRepository
import com.nextstep.app.data.repository.room.RoomStudyPlanRepository
import com.nextstep.app.data.repository.room.RoomStudySessionRepository
import com.nextstep.app.data.repository.room.RoomSubjectRepository
import com.nextstep.app.data.repository.room.RoomTaskRepository
import com.nextstep.app.data.repository.room.RoomTopicRepository
import com.nextstep.app.data.sync.FirestoreSyncManager
import com.nextstep.app.data.sync.NoOpSyncManager
import com.nextstep.app.data.sync.SyncManager
import com.nextstep.app.data.sync.SyncRegistry

/**
 * 수동 의존성 조립. 구현체를 아는 곳은 여기뿐이고, 나머지는 인터페이스만 봅니다.
 */
class AppContainer(context: Context) {
    private val database: AppDatabase = AppDatabase.get(context)
    private val preferences = UserPreferences(context)
    private val time: TimeSource = SystemTimeSource
    private val scope: FamilyScope = PrefsFamilyScope(preferences)

    /** google-services.json 이 있어 FirebaseApp 이 초기화됐을 때만 원격 동기화를 켭니다. */
    val syncManager: SyncManager = createSyncManager(context, database)

    val onboarding: OnboardingRepository = RoomOnboardingRepository(preferences, database.memberDao(), database.subjectDao(), syncManager)
    val members: MemberRepository = RoomMemberRepository(database.memberDao(), scope, syncManager, time)
    val subjects: SubjectRepository = RoomSubjectRepository(database.subjectDao(), database.topicDao(), scope, syncManager, time)
    val topics: TopicRepository = RoomTopicRepository(database.topicDao(), scope, syncManager, time)
    val tasks: TaskRepository = RoomTaskRepository(database.taskDao(), scope, syncManager, time)
    val events: EventRepository = RoomEventRepository(database.eventDao(), scope, syncManager, time)
    val grades: GradeRepository = RoomGradeRepository(database.gradeDao(), scope, syncManager, time)
    val sessions: StudySessionRepository = RoomStudySessionRepository(database.studySessionDao(), preferences, scope, syncManager, time)
    val notes: NoteRepository = RoomNoteRepository(database.noteDao(), scope, syncManager, time)
    val roadmap: RoadmapRepository = RoomRoadmapRepository(database.roadmapDao(), scope, syncManager, time)
    val contents: ContentRepository = RoomContentRepository(database.contentDao(), database.subjectDao(), YouTubeMetadataFetcher(), scope, syncManager, time)
    val journey: JourneyRepository = RoomJourneyRepository(database.journeyDao(), scope, syncManager, time)
    val goals: GoalRepository = RoomGoalRepository(database.goalDao(), database.goalStepDao(), scope, syncManager, time)
    val plans: StudyPlanRepository = RoomStudyPlanRepository(database.eventDao(), database.taskDao(), scope, syncManager, time)
    val streams: FamilyDataStreams = CompositeFamilyDataStreams(onboarding, subjects, topics, tasks, events, grades, sessions, notes, members, roadmap, contents, journey, goals)

    private fun createSyncManager(context: Context, db: AppDatabase): SyncManager {
        if (FirebaseApp.getApps(context).isEmpty()) {
            Log.i(TAG, "Firebase 미설정: 로컬 전용 모드")
            return NoOpSyncManager()
        }
        return try {
            FirestoreSyncManager(SyncRegistry.familyCollections(db), SyncRegistry.catalogCollection(db), FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
        } catch (e: Exception) {
            Log.w(TAG, "Firebase 초기화 실패, 로컬 전용 모드로 동작합니다", e)
            NoOpSyncManager()
        }
    }

    private companion object {
        const val TAG = "AppContainer"
    }
}
