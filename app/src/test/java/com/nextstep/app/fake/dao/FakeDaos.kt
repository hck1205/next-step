package com.nextstep.app.fake.dao

import com.nextstep.app.data.local.dao.ContentDao
import com.nextstep.app.data.local.dao.EventDao
import com.nextstep.app.data.local.dao.GradeDao
import com.nextstep.app.data.local.dao.GoalDao
import com.nextstep.app.data.local.dao.GoalStepDao
import com.nextstep.app.data.local.dao.JourneyDao
import com.nextstep.app.data.local.dao.MemberDao
import com.nextstep.app.data.local.dao.NoteDao
import com.nextstep.app.data.local.dao.RoadmapDao
import com.nextstep.app.data.local.dao.StudySessionDao
import com.nextstep.app.data.local.dao.SubjectDao
import com.nextstep.app.data.local.dao.TaskDao
import com.nextstep.app.data.local.dao.TopicDao
import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.local.entity.JourneyItemEntity
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.ContentScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FakeSubjectDao : InMemoryTable<SubjectEntity>(), SubjectDao {
    override suspend fun getAll(familyId: String) = all.filter { it.familyId == familyId && !it.deleted }.sortedBy { it.orderIndex }
    override fun observeById(id: String): Flow<SubjectEntity?> = rows.map { it[id] }
    override suspend fun count(familyId: String) = all.count { it.familyId == familyId && !it.deleted }
}

class FakeTopicDao : InMemoryTable<TopicEntity>(), TopicDao {
    override fun observeBySubject(subjectId: String): Flow<List<TopicEntity>> = rows.map { m -> m.values.filter { it.subjectId == subjectId && !it.deleted }.sortedBy { it.orderIndex } }
    override suspend fun getBySubject(subjectId: String) = all.filter { it.subjectId == subjectId && !it.deleted }.sortedBy { it.orderIndex }
}

class FakeTaskDao : InMemoryTable<TaskEntity>(), TaskDao
class FakeEventDao : InMemoryTable<EventEntity>(), EventDao
class FakeGradeDao : InMemoryTable<GradeEntity>(), GradeDao
class FakeNoteDao : InMemoryTable<NoteEntity>(), NoteDao

class FakeStudySessionDao : InMemoryTable<StudySessionEntity>(), StudySessionDao {
    override fun observeSince(familyId: String, fromMillis: Long): Flow<List<StudySessionEntity>> = observeAll(familyId).map { l -> l.filter { it.startAt >= fromMillis } }
}

class FakeGoalDao : InMemoryTable<GoalEntity>(), GoalDao

class FakeGoalStepDao : InMemoryTable<GoalStepEntity>(), GoalStepDao {
    override suspend fun getByGoal(goalId: String) = all.filter { it.goalId == goalId && !it.deleted }
}

class FakeJourneyDao : InMemoryTable<JourneyItemEntity>(), JourneyDao {
    override suspend fun getByTemplate(familyId: String, templateId: String) = all.firstOrNull { it.familyId == familyId && it.templateId == templateId && !it.deleted }
}

class FakeMemberDao : InMemoryTable<MemberEntity>(), MemberDao {
    override fun observeById(id: String): Flow<MemberEntity?> = rows.map { it[id] }
}

class FakeRoadmapDao : InMemoryTable<RoadmapItemEntity>(), RoadmapDao {
    override suspend fun count(familyId: String) = all.count { it.familyId == familyId && !it.deleted }
}

class FakeContentDao : InMemoryTable<ContentEntity>(), ContentDao {
    override fun observeAll(familyId: String): Flow<List<ContentEntity>> = rows.map { m -> m.values.filter { (it.familyId == familyId || it.scope == ContentScope.GLOBAL) && !it.deleted } }
    override suspend fun findByVideoId(familyId: String, videoId: String) = all.firstOrNull { (it.familyId == familyId || it.scope == ContentScope.GLOBAL) && it.videoId == videoId && !it.deleted }
    override suspend fun getDirty(familyId: String) = all.filter { it.familyId == familyId && it.scope == ContentScope.FAMILY && it.dirty }
}
