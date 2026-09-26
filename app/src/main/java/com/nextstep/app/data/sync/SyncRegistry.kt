package com.nextstep.app.data.sync

import com.nextstep.app.data.local.AppDatabase
import com.nextstep.app.data.sync.mapper.ActivityMapper
import com.nextstep.app.data.sync.mapper.ContentMapper
import com.nextstep.app.data.sync.mapper.EventMapper
import com.nextstep.app.data.sync.mapper.GoalMapper
import com.nextstep.app.data.sync.mapper.GoalStepMapper
import com.nextstep.app.data.sync.mapper.GradeMapper
import com.nextstep.app.data.sync.mapper.GrowthRecordMapper
import com.nextstep.app.data.sync.mapper.JourneyItemMapper
import com.nextstep.app.data.sync.mapper.MemberMapper
import com.nextstep.app.data.sync.mapper.ObservationMapper
import com.nextstep.app.data.sync.mapper.PeerTopicMapper
import com.nextstep.app.data.sync.mapper.ProjectLogMapper
import com.nextstep.app.data.sync.mapper.RoadmapItemMapper
import com.nextstep.app.data.sync.mapper.StudySessionMapper
import com.nextstep.app.data.sync.mapper.SubjectMapper
import com.nextstep.app.data.sync.mapper.TaskMapper
import com.nextstep.app.data.sync.mapper.TopicMapper
import com.nextstep.app.data.sync.mapper.WeekPlanMapper

/**
 * 가족 단위로 동기화하는 컬렉션 목록. 새 엔티티는 여기 한 줄만 추가하면 수신·전송 모두 붙습니다.
 * DAO 가 SyncDao 를 구현하므로 매퍼와 DAO 만 짝지으면 됩니다.
 */
object SyncRegistry {
    fun familyCollections(db: AppDatabase): List<SyncedCollection<*>> = listOf(
        SyncedCollection.of(SubjectMapper, db.subjectDao()),
        SyncedCollection.of(TopicMapper, db.topicDao()),
        SyncedCollection.of(TaskMapper, db.taskDao()),
        SyncedCollection.of(EventMapper, db.eventDao()),
        SyncedCollection.of(GradeMapper, db.gradeDao()),
        SyncedCollection.of(StudySessionMapper, db.studySessionDao()),
        SyncedCollection.of(MemberMapper, db.memberDao()),
        SyncedCollection.of(RoadmapItemMapper, db.roadmapDao()),
        SyncedCollection.of(ContentMapper.Family, db.contentDao()),
        SyncedCollection.of(JourneyItemMapper, db.journeyDao()),
        SyncedCollection.of(GoalMapper, db.goalDao()),
        SyncedCollection.of(GoalStepMapper, db.goalStepDao()),
        SyncedCollection.of(ActivityMapper, db.activityDao()),
        SyncedCollection.of(GrowthRecordMapper, db.growthRecordDao()),
        SyncedCollection.of(ObservationMapper, db.observationDao()),
        SyncedCollection.of(ProjectLogMapper, db.projectLogDao()),
        SyncedCollection.of(WeekPlanMapper, db.weekPlanDao()),
    )

    /** 최상위 공용 컬렉션(읽기 전용) 전부. 새 공용 데이터는 여기 한 줄. */
    fun globalCollections(db: AppDatabase): List<SyncedCollection<*>> = listOf(catalogCollection(db), peerTopicsCollection(db))

    /** 서버가 집계한 또래 단원 통계. 읽기만 합니다. */
    fun peerTopicsCollection(db: AppDatabase): SyncedCollection<*> =
        SyncedCollection.readOnly(PeerTopicMapper, db.peerTopicDao()::getById, db.peerTopicDao()::upsert)

    /** 운영자가 큐레이팅하는 공용 콘텐츠 저장소. 읽기 전용이며 기기 로컬의 시청 표시는 보존합니다. */
    fun catalogCollection(db: AppDatabase): SyncedCollection<*> = SyncedCollection.readOnly(
        ContentMapper.Catalog, db.contentDao()::getById, db.contentDao()::upsert,
        reconcile = { remote, local -> remote.copy(watched = local?.watched ?: false) },
    )
}
