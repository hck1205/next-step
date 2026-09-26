package com.nextstep.app.fake

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.data.local.entity.ObservationEntity
import com.nextstep.app.data.local.entity.ProjectLogEntity
import com.nextstep.app.data.local.entity.JourneyItemEntity
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.data.local.entity.RewardEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.data.prefs.RunningTimer
import com.nextstep.app.data.prefs.UserProfile
import com.nextstep.app.data.repository.FamilyDataStreams
import kotlinx.coroutines.flow.MutableStateFlow

/** 테스트에서 값을 직접 밀어 넣는 읽기 스트림. */
class FakeFamilyDataStreams(
    role: Role = Role.STUDENT,
    familyId: String = "fam",
) : FamilyDataStreams {
    override val profile = MutableStateFlow(UserProfile(role, "테스터", familyId, "ABC123", "학생", onboarded = true, memberId = "me"))
    override val syncStatus = MutableStateFlow(SyncStatus.LOCAL_ONLY)
    override val subjects = MutableStateFlow<List<SubjectEntity>>(emptyList())
    override val topics = MutableStateFlow<List<TopicEntity>>(emptyList())
    override val tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    override val events = MutableStateFlow<List<EventEntity>>(emptyList())
    override val grades = MutableStateFlow<List<GradeEntity>>(emptyList())
    override val sessions = MutableStateFlow<List<StudySessionEntity>>(emptyList())
    override val members = MutableStateFlow<List<MemberEntity>>(emptyList())
    override val myMember = MutableStateFlow<MemberEntity?>(null)
    override val roadmap = MutableStateFlow<List<RoadmapItemEntity>>(emptyList())
    override val contents = MutableStateFlow<List<ContentEntity>>(emptyList())
    override val runningTimer = MutableStateFlow<RunningTimer?>(null)
    override val journeyItems = MutableStateFlow<List<JourneyItemEntity>>(emptyList())
    override val goals = MutableStateFlow<List<GoalEntity>>(emptyList())
    override val goalSteps = MutableStateFlow<List<GoalStepEntity>>(emptyList())
    override val activities = MutableStateFlow<List<ActivityEntity>>(emptyList())
    override val growthRecords = MutableStateFlow<List<GrowthRecordEntity>>(emptyList())
    override val observations = MutableStateFlow<List<ObservationEntity>>(emptyList())
    override val projectLogs = MutableStateFlow<List<ProjectLogEntity>>(emptyList())
    override val weekPlans = MutableStateFlow<List<WeekPlanEntity>>(emptyList())
    override val rewards = MutableStateFlow<List<RewardEntity>>(emptyList())
}
