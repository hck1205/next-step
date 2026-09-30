package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.local.entity.CheerEntity
import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.FamilyEventEntity
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.data.local.entity.JourneyItemEntity
import com.nextstep.app.data.local.entity.LessonEntity
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.ObservationEntity
import com.nextstep.app.data.local.entity.ProjectLogEntity
import com.nextstep.app.data.local.entity.ReportLogEntity
import com.nextstep.app.data.local.entity.RewardEntity
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.local.entity.WeekPlanEntity
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
    val members: Flow<List<MemberEntity>>
    val myMember: Flow<MemberEntity?>
    val roadmap: Flow<List<RoadmapItemEntity>>
    val contents: Flow<List<ContentEntity>>
    val runningTimer: Flow<RunningTimer?>
    val journeyItems: Flow<List<JourneyItemEntity>>
    val goals: Flow<List<GoalEntity>>
    val goalSteps: Flow<List<GoalStepEntity>>
    val activities: Flow<List<ActivityEntity>>
    val growthRecords: Flow<List<GrowthRecordEntity>>
    val observations: Flow<List<ObservationEntity>>
    val projectLogs: Flow<List<ProjectLogEntity>>
    val weekPlans: Flow<List<WeekPlanEntity>>
    val rewards: Flow<List<RewardEntity>>
    /** 가족 달력의 일정. 멘토에게는 늘 비어 있습니다(MentorScopedStreams). */
    val familyEvents: Flow<List<FamilyEventEntity>>
    /** 해낸 일에 붙인 응원. 멘토에게는 늘 비어 있습니다(가족의 일). */
    val cheers: Flow<List<CheerEntity>>
    /** 수업 리포트 보낸 기록. 멘토에게는 자기가 보낸 것만. */
    val reportLogs: Flow<List<ReportLogEntity>>
    /** 수업 출결 기록. 멘토에게는 자기 수업만. */
    val lessons: Flow<List<LessonEntity>>
}
