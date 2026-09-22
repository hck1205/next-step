package com.nextstep.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

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
    ],
    version = 4,
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
