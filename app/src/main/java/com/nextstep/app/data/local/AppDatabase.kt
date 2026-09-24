package com.nextstep.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nextstep.app.data.local.dao.RoadmapDao
import com.nextstep.app.data.local.dao.NoteDao
import com.nextstep.app.data.local.dao.StudySessionDao
import com.nextstep.app.data.local.dao.TaskDao
import com.nextstep.app.data.local.dao.GradeDao
import com.nextstep.app.data.local.dao.TopicDao
import com.nextstep.app.data.local.dao.EventDao
import com.nextstep.app.data.local.dao.SubjectDao
import com.nextstep.app.data.local.dao.ContentDao
import com.nextstep.app.data.local.dao.MemberDao
import com.nextstep.app.data.local.dao.JourneyDao
import com.nextstep.app.data.local.dao.ActivityDao
import com.nextstep.app.data.local.dao.GrowthRecordDao
import com.nextstep.app.data.local.dao.PeerTopicDao
import com.nextstep.app.data.local.entity.PeerTopicEntity
import com.nextstep.app.data.local.dao.ObservationDao
import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.data.local.entity.ObservationEntity
import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.local.dao.GoalDao
import com.nextstep.app.data.local.dao.GoalStepDao
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.local.entity.JourneyItemEntity
import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.ContentEntity

@Database(
    entities = [
        SubjectEntity::class,
        TopicEntity::class,
        TaskEntity::class,
        EventEntity::class,
        GradeEntity::class,
        StudySessionEntity::class,
        NoteEntity::class,
        MemberEntity::class,
        RoadmapItemEntity::class,
        ContentEntity::class,
        JourneyItemEntity::class,
        GoalEntity::class,
        GoalStepEntity::class,
        ActivityEntity::class,
        GrowthRecordEntity::class,
        ObservationEntity::class,
        PeerTopicEntity::class,
    ],
    version = 11,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun topicDao(): TopicDao
    abstract fun taskDao(): TaskDao
    abstract fun eventDao(): EventDao
    abstract fun gradeDao(): GradeDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun noteDao(): NoteDao
    abstract fun memberDao(): MemberDao
    abstract fun roadmapDao(): RoadmapDao
    abstract fun contentDao(): ContentDao
    abstract fun journeyDao(): JourneyDao
    abstract fun goalDao(): GoalDao
    abstract fun goalStepDao(): GoalStepDao
    abstract fun activityDao(): ActivityDao
    abstract fun growthRecordDao(): GrowthRecordDao
    abstract fun observationDao(): ObservationDao
    abstract fun peerTopicDao(): PeerTopicDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "nextstep.db")
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}
