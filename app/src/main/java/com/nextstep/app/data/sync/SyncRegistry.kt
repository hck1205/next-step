package com.nextstep.app.data.sync

import com.nextstep.app.data.local.AppDatabase
import com.nextstep.app.data.sync.mapper.ContentMapper
import com.nextstep.app.data.sync.mapper.EventMapper
import com.nextstep.app.data.sync.mapper.GradeMapper
import com.nextstep.app.data.sync.mapper.MemberMapper
import com.nextstep.app.data.sync.mapper.NoteMapper
import com.nextstep.app.data.sync.mapper.RoadmapItemMapper
import com.nextstep.app.data.sync.mapper.StudySessionMapper
import com.nextstep.app.data.sync.mapper.SubjectMapper
import com.nextstep.app.data.sync.mapper.TaskMapper
import com.nextstep.app.data.sync.mapper.TopicMapper

/**
 * 가족 단위로 동기화하는 컬렉션 목록. 새 엔티티는 여기 한 줄만 추가하면 수신·전송 모두 붙습니다.
 */
object SyncRegistry {
    fun familyCollections(db: AppDatabase): List<SyncedCollection<*>> = listOf(
        SyncedCollection(SubjectMapper, db.subjectDao()::getById, db.subjectDao()::upsert, db.subjectDao()::getDirty, db.subjectDao()::markClean),
        SyncedCollection(TopicMapper, db.topicDao()::getById, db.topicDao()::upsert, db.topicDao()::getDirty, db.topicDao()::markClean),
        SyncedCollection(TaskMapper, db.taskDao()::getById, db.taskDao()::upsert, db.taskDao()::getDirty, db.taskDao()::markClean),
        SyncedCollection(EventMapper, db.eventDao()::getById, db.eventDao()::upsert, db.eventDao()::getDirty, db.eventDao()::markClean),
        SyncedCollection(GradeMapper, db.gradeDao()::getById, db.gradeDao()::upsert, db.gradeDao()::getDirty, db.gradeDao()::markClean),
        SyncedCollection(StudySessionMapper, db.studySessionDao()::getById, db.studySessionDao()::upsert, db.studySessionDao()::getDirty, db.studySessionDao()::markClean),
        SyncedCollection(NoteMapper, db.noteDao()::getById, db.noteDao()::upsert, db.noteDao()::getDirty, db.noteDao()::markClean),
        SyncedCollection(MemberMapper, db.memberDao()::getById, db.memberDao()::upsert, db.memberDao()::getDirty, db.memberDao()::markClean),
        SyncedCollection(RoadmapItemMapper, db.roadmapDao()::getById, db.roadmapDao()::upsert, db.roadmapDao()::getDirty, db.roadmapDao()::markClean),
        SyncedCollection(ContentMapper.Family, db.contentDao()::getById, db.contentDao()::upsert, db.contentDao()::getDirty, db.contentDao()::markClean),
    )

    /** 운영자가 큐레이팅하는 공용 콘텐츠 저장소. 읽기 전용이며 기기 로컬의 시청 표시는 보존합니다. */
    fun catalogCollection(db: AppDatabase): SyncedCollection<*> = SyncedCollection(
        ContentMapper.Catalog, db.contentDao()::getById, db.contentDao()::upsert,
        getDirty = { emptyList() }, markClean = {},
        reconcile = { remote, local -> remote.copy(watched = local?.watched ?: false) },
    )
}
