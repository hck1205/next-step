package com.nextstep.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE familyId = :familyId AND deleted = 0 ORDER BY orderIndex, name")
    fun observeAll(familyId: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE familyId = :familyId AND deleted = 0 ORDER BY orderIndex, name")
    suspend fun getAll(familyId: String): List<SubjectEntity>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getById(id: String): SubjectEntity?

    @Query("SELECT * FROM subjects WHERE id = :id")
    fun observeById(id: String): Flow<SubjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SubjectEntity>)

    @Query("SELECT * FROM subjects WHERE familyId = :familyId AND dirty = 1")
    suspend fun getDirty(familyId: String): List<SubjectEntity>

    @Query("UPDATE subjects SET dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<String>)

    @Query("SELECT COUNT(*) FROM subjects WHERE familyId = :familyId AND deleted = 0")
    suspend fun count(familyId: String): Int
}

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics WHERE familyId = :familyId AND deleted = 0 ORDER BY subjectId, orderIndex")
    fun observeAll(familyId: String): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId AND deleted = 0 ORDER BY orderIndex")
    fun observeBySubject(subjectId: String): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId AND deleted = 0 ORDER BY orderIndex")
    suspend fun getBySubject(subjectId: String): List<TopicEntity>

    @Query("SELECT * FROM topics WHERE id = :id")
    suspend fun getById(id: String): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: TopicEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<TopicEntity>)

    @Query("SELECT * FROM topics WHERE familyId = :familyId AND dirty = 1")
    suspend fun getDirty(familyId: String): List<TopicEntity>

    @Query("UPDATE topics SET dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<String>)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE familyId = :familyId AND deleted = 0 ORDER BY done, dueDate, updatedAt DESC")
    fun observeAll(familyId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<TaskEntity>)

    @Query("SELECT * FROM tasks WHERE familyId = :familyId AND dirty = 1")
    suspend fun getDirty(familyId: String): List<TaskEntity>

    @Query("UPDATE tasks SET dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<String>)
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE familyId = :familyId AND deleted = 0 ORDER BY startAt")
    fun observeAll(familyId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: String): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<EventEntity>)

    @Query("SELECT * FROM events WHERE familyId = :familyId AND dirty = 1")
    suspend fun getDirty(familyId: String): List<EventEntity>

    @Query("UPDATE events SET dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<String>)
}

@Dao
interface GradeDao {
    @Query("SELECT * FROM grades WHERE familyId = :familyId AND deleted = 0 ORDER BY date DESC, updatedAt DESC")
    fun observeAll(familyId: String): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE id = :id")
    suspend fun getById(id: String): GradeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: GradeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<GradeEntity>)

    @Query("SELECT * FROM grades WHERE familyId = :familyId AND dirty = 1")
    suspend fun getDirty(familyId: String): List<GradeEntity>

    @Query("UPDATE grades SET dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<String>)
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions WHERE familyId = :familyId AND deleted = 0 ORDER BY startAt DESC")
    fun observeAll(familyId: String): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE familyId = :familyId AND deleted = 0 AND startAt >= :fromMillis ORDER BY startAt DESC")
    fun observeSince(familyId: String, fromMillis: Long): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE id = :id")
    suspend fun getById(id: String): StudySessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: StudySessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<StudySessionEntity>)

    @Query("SELECT * FROM study_sessions WHERE familyId = :familyId AND dirty = 1")
    suspend fun getDirty(familyId: String): List<StudySessionEntity>

    @Query("UPDATE study_sessions SET dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<String>)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE familyId = :familyId AND deleted = 0 ORDER BY createdAt DESC")
    fun observeAll(familyId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<NoteEntity>)

    @Query("SELECT * FROM notes WHERE familyId = :familyId AND dirty = 1")
    suspend fun getDirty(familyId: String): List<NoteEntity>

    @Query("UPDATE notes SET dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<String>)
}
