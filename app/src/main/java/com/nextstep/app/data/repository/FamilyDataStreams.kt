package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.JourneyItemEntity
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.data.prefs.RunningTimer
import com.nextstep.app.data.prefs.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * 읽기 전용 스트림 모음. 여러 애그리거트를 한꺼번에 보는 화면(대시보드, 분석)은 이것만 의존합니다.
 * 쓰기는 각 XxxRepository 로 합니다.
 */
interface FamilyDataStreams {
    val profile: Flow<UserProfile>
    val syncStatus: StateFlow<SyncStatus>
    val subjects: Flow<List<SubjectEntity>>
    val topics: Flow<List<TopicEntity>>
    val tasks: Flow<List<TaskEntity>>
    val events: Flow<List<EventEntity>>
    val grades: Flow<List<GradeEntity>>
    val sessions: Flow<List<StudySessionEntity>>
    val notes: Flow<List<NoteEntity>>
    val members: Flow<List<MemberEntity>>
    val myMember: Flow<MemberEntity?>
    val roadmap: Flow<List<RoadmapItemEntity>>
    val contents: Flow<List<ContentEntity>>
    val runningTimer: Flow<RunningTimer?>
    val journeyItems: Flow<List<JourneyItemEntity>>
    val goals: Flow<List<GoalEntity>>
    val goalSteps: Flow<List<GoalStepEntity>>
    val activities: Flow<List<ActivityEntity>>
}
