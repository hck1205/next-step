package com.nextstep.app.fake

import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.data.local.entity.ObservationEntity
import com.nextstep.app.data.local.entity.PeerTopicEntity
import com.nextstep.app.data.local.entity.JourneyItemEntity
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.ProjectLogEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.data.prefs.RunningTimer
import com.nextstep.app.data.prefs.UserProfile
import com.nextstep.app.data.repository.ActivityRepository
import com.nextstep.app.data.repository.ContentDraft
import com.nextstep.app.data.repository.ContentRepository
import com.nextstep.app.data.repository.EventRepository
import com.nextstep.app.data.repository.GoalRepository
import com.nextstep.app.data.repository.GrowthRepository
import com.nextstep.app.data.repository.JourneyRepository
import com.nextstep.app.data.repository.MemberRepository
import com.nextstep.app.data.repository.OnboardingRepository
import com.nextstep.app.data.repository.PeerCurriculumRepository
import com.nextstep.app.data.repository.ProjectRepository
import com.nextstep.app.data.repository.RoadmapRepository
import com.nextstep.app.data.repository.StudyPlanRepository
import com.nextstep.app.data.repository.StudySessionRepository
import com.nextstep.app.data.repository.TopicRepository
import com.nextstep.app.data.repository.WeekPlanRepository
import com.nextstep.app.data.sync.FamilyInfo
import com.nextstep.app.domain.content.ContentClassification
import com.nextstep.app.domain.planner.StudyPlan
import com.nextstep.app.domain.selfdirection.SelfDirectionStage
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** 호출을 기록하고 스트림에 반영하는 단순 Fake 들. 한 파일에 모아 두었지만 각자 독립적으로 씁니다. */

class FakeTopicRepository : TopicRepository {
    override val topics = MutableStateFlow<List<TopicEntity>>(emptyList())
    val calls = mutableListOf<String>()
    override fun observeBySubject(subjectId: String): Flow<List<TopicEntity>> = topics.map { l -> l.filter { it.subjectId == subjectId } }
    override suspend fun add(subjectId: String, titles: List<String>) { calls += "add:$subjectId:${titles.joinToString("|")}" }
    override suspend fun update(topic: TopicEntity) { calls += "update:${topic.id}"; topics.value = topics.value.filter { it.id != topic.id } + topic }
    override suspend fun setStatus(id: String, status: TopicStatus) { calls += "status:$id:$status" }
    override suspend fun setClassProgress(subjectId: String, upToOrderIndex: Int) { calls += "class:$subjectId:$upToOrderIndex" }
    override suspend fun delete(id: String) { calls += "delete:$id" }
}

class FakeEventRepository : EventRepository {
    override val events = MutableStateFlow<List<EventEntity>>(emptyList())
    val saved = mutableListOf<EventEntity>()
    val deleted = mutableListOf<String>()
    override suspend fun save(event: EventEntity) { saved += event; events.value = events.value.filter { it.id != event.id } + event }
    override suspend fun delete(id: String) { deleted += id }
}

/** [streams] 를 주면 타이머 상태를 그 파사드와 공유해, 실제 앱처럼 저장소 쓰기가 스트림 읽기에 반영됩니다. */
class FakeStudySessionRepository(streams: FakeFamilyDataStreams? = null) : StudySessionRepository {
    override val sessions = streams?.sessions ?: MutableStateFlow(emptyList())
    override val runningTimer: MutableStateFlow<RunningTimer?> = streams?.runningTimer ?: MutableStateFlow(null)
    val saved = mutableListOf<StudySessionEntity>()
    val deleted = mutableListOf<String>()
    var stopResult: StudySessionEntity? = null
    override suspend fun save(session: StudySessionEntity) { saved += session }
    override suspend fun delete(id: String) { deleted += id }
    override suspend fun startTimer(subjectId: String?) { runningTimer.value = RunningTimer(subjectId, 0L) }
    override suspend fun stopTimer(note: String): StudySessionEntity? { runningTimer.value = null; return stopResult }
    override suspend fun cancelTimer() { runningTimer.value = null }
}

class FakeRoadmapRepository : RoadmapRepository {
    override val roadmap = MutableStateFlow<List<RoadmapItemEntity>>(emptyList())
    val saved = mutableListOf<RoadmapItemEntity>()
    val statuses = mutableListOf<Pair<String, RoadmapStatus>>()
    val deleted = mutableListOf<String>()
    override suspend fun save(item: RoadmapItemEntity) { saved += item }
    override suspend fun setStatus(id: String, status: RoadmapStatus) { statuses += id to status }
    override suspend fun delete(id: String) { deleted += id }
}

class FakeContentRepository : ContentRepository {
    override val contents = MutableStateFlow<List<ContentEntity>>(emptyList())
    var prepareResult: Result<ContentDraft> = Result.failure(IllegalStateException("not configured"))
    val saved = mutableListOf<ContentEntity>()
    val rated = mutableListOf<Pair<String, Int>>()
    val watched = mutableListOf<Pair<String, Boolean>>()
    val deleted = mutableListOf<String>()
    override suspend fun prepare(url: String): Result<ContentDraft> = prepareResult
    override suspend fun save(content: ContentEntity) { saved += content }
    override suspend fun rate(id: String, stars: Int) { rated += id to stars }
    override suspend fun setWatched(id: String, watched: Boolean) { this.watched += id to watched }
    override suspend fun delete(id: String) { deleted += id }

    companion object {
        fun draft(url: String = "https://www.youtube.com/watch?v=abcdefghijk", title: String = "제목") = ContentDraft(
            url = url, videoId = "abcdefghijk", title = title, channel = "채널", thumbnailUrl = "",
            classification = ContentClassification("수학", com.nextstep.app.data.model.GradeLevel.MIDDLE, com.nextstep.app.data.model.ContentType.CONCEPT, listOf("방정식"), listOf("근거")),
            metadataFetched = true,
        )
    }
}

class FakeStudyPlanRepository : StudyPlanRepository {
    val applied = mutableListOf<StudyPlan>()
    override suspend fun apply(plan: StudyPlan) { applied += plan }
}

class FakeMemberRepository : MemberRepository {
    override val members = MutableStateFlow<List<MemberEntity>>(emptyList())
    override val myMember = MutableStateFlow<MemberEntity?>(null)
    val calls = mutableListOf<String>()
    override suspend fun setSubjects(memberId: String, subjectIds: List<String>) { calls += "subjects:$memberId:${subjectIds.joinToString("|")}" }
    override suspend fun updateProfile(memberId: String, name: String, title: String) { calls += "profile:$memberId:$name:$title" }
    override suspend fun setMentorEnabled(memberId: String, enabled: Boolean) { calls += "mentor:$memberId:$enabled" }
    override suspend fun setGradeYear(memberId: String, gradeYear: Int) { calls += "grade:$memberId:$gradeYear" }
    override suspend fun setBirthDate(memberId: String, birthDate: java.time.LocalDate?) { calls += "birth:$memberId:$birthDate" }
    override suspend fun setUiLevel(memberId: String, level: StudentUiLevel?) { calls += "uiLevel:$memberId:${level?.name}" }
    override suspend fun markUiLevelSeen(memberId: String, level: StudentUiLevel) { calls += "seen:$memberId:${level.name}" }
    override suspend fun setSelfDirection(memberId: String, stage: SelfDirectionStage?) { calls += "self:$memberId:${stage?.name}" }
    override suspend fun remove(memberId: String) { calls += "remove:$memberId" }
}

class FakeOnboardingRepository(override val syncAvailable: Boolean = true) : OnboardingRepository {
    override val profile = MutableStateFlow(UserProfile(null, "", null, null, "", onboarded = false, memberId = null))
    override val syncStatus = MutableStateFlow(SyncStatus.LOCAL_ONLY)
    var createResult: Result<FamilyInfo> = Result.success(FamilyInfo("fam", "ABC123", "학생"))
    var joinResult: Result<FamilyInfo> = Result.success(FamilyInfo("fam", "ABC123", "학생"))
    val calls = mutableListOf<String>()
    override suspend fun createFamilyAsStudent(studentName: String, gradeYear: Int, birthDate: java.time.LocalDate?): Result<FamilyInfo> { calls += "create:$studentName:$gradeYear" + (birthDate?.let { ":$it" } ?: ""); return createResult }
    override suspend fun createFamilyAsParent(parentName: String, childName: String, birthDate: java.time.LocalDate?, relation: String): Result<FamilyInfo> { calls += "createAsParent:$parentName:$childName:$birthDate" + (if (relation.isNotEmpty()) ":$relation" else ""); return createResult }
    override suspend fun addChildAsParent(childName: String, birthDate: java.time.LocalDate?): Result<FamilyInfo> { calls += "addChild:$childName:$birthDate"; return createResult }
    override suspend fun linkChild(code: String): Result<FamilyInfo> { calls += "linkChild:$code"; return joinResult }
    override suspend fun switchChild(familyId: String) { calls += "switch:$familyId" }
    override suspend fun joinFamily(role: Role, name: String, code: String, title: String): Result<FamilyInfo> { calls += "join:$role:$name:$code:$title"; return joinResult }
    override suspend fun resumeSync() { calls += "resume" }
    override suspend fun signOut() { calls += "signOut" }
    override fun requestSync() { calls += "requestSync" }
}

class FakeJourneyRepository : JourneyRepository {
    override val items = MutableStateFlow<List<JourneyItemEntity>>(emptyList())
    val calls = mutableListOf<String>()
    override suspend fun setTemplateStatus(templateId: String, status: MilestoneStatus, dueDate: LocalDate) { calls += "tStatus:$templateId:$status:$dueDate" }
    override suspend fun setTemplateNote(templateId: String, note: String, dueDate: LocalDate) { calls += "tNote:$templateId:$note" }
    override suspend fun setTemplateDueDate(templateId: String, dueDate: LocalDate) { calls += "tDue:$templateId:$dueDate" }
    override suspend fun addCustom(title: String, description: String, category: String, dueDate: LocalDate, leadMonths: Int, priority: Int) { calls += "add:$title:$category:$dueDate:$leadMonths" }
    override suspend fun update(item: JourneyItemEntity) { calls += "update:${item.id}" }
    override suspend fun setStatus(id: String, status: MilestoneStatus) { calls += "status:$id:$status" }
    override suspend fun setNote(id: String, note: String) { calls += "note:$id:$note" }
    override suspend fun setDueDate(id: String, dueDate: LocalDate) { calls += "due:$id:$dueDate" }
    override suspend fun delete(id: String) { calls += "delete:$id" }
}

class FakeGoalRepository : GoalRepository {
    override val goals = MutableStateFlow<List<GoalEntity>>(emptyList())
    override val steps = MutableStateFlow<List<GoalStepEntity>>(emptyList())
    val calls = mutableListOf<String>()
    val addedGoals = mutableListOf<GoalEntity>()
    val addedSteps = mutableListOf<GoalStepEntity>()
    override suspend fun add(goal: GoalEntity, steps: List<GoalStepEntity>) { calls += "add:${goal.trackId ?: goal.title}:${steps.size}"; addedGoals += goal; addedSteps += steps }
    override suspend fun addStep(step: GoalStepEntity) { calls += "addStep:${step.goalId}:${step.periodKey}:${step.title}"; addedSteps += step }
    override suspend fun setStepStatus(stepId: String, status: MilestoneStatus) { calls += "stepStatus:$stepId:$status" }
    override suspend fun setStepTask(stepId: String, taskId: String?) { calls += "stepTask:$stepId:${if (taskId == null) "null" else "set"}" }
    override suspend fun setGoalStatus(goalId: String, status: GoalStatus) { calls += "goalStatus:$goalId:$status" }
    override suspend fun link(goalId: String, leadsTo: String?) { calls += "link:$goalId:$leadsTo" }
    override suspend fun edit(goalId: String, title: String, description: String, targetDate: Long?) { calls += "edit:$goalId:$title:$description:$targetDate" }
    override suspend fun delete(goalId: String) { calls += "delete:$goalId" }
}

class FakeActivityRepository : ActivityRepository {
    override val activities = MutableStateFlow<List<ActivityEntity>>(emptyList())
    val saved = mutableListOf<ActivityEntity>()
    val deleted = mutableListOf<String>()
    override suspend fun save(activity: ActivityEntity) { saved += activity }
    override suspend fun delete(id: String) { deleted += id }
}

class FakeGrowthRepository : GrowthRepository {
    override val records = MutableStateFlow<List<GrowthRecordEntity>>(emptyList())
    override val observations = MutableStateFlow<List<ObservationEntity>>(emptyList())
    val calls = mutableListOf<String>()
    override suspend fun saveRecord(record: GrowthRecordEntity) { calls += "record:${record.heightCm}:${record.weightKg}:${record.visionLeft}:${record.visionRight}" }
    override suspend fun deleteRecord(id: String) { calls += "deleteRecord:$id" }
    override suspend fun addObservation(observation: ObservationEntity) { calls += "observe:${observation.domain}:${observation.strength}:${observation.text}" }
    override suspend fun deleteObservation(id: String) { calls += "deleteObservation:$id" }
}

/** [streams] 를 주면 기록이 그 파사드의 projectLogs 에도 보여, 실제 앱처럼 쓰기가 읽기에 반영됩니다. */
class FakeProjectRepository(streams: FakeFamilyDataStreams? = null) : ProjectRepository {
    override val logs: MutableStateFlow<List<ProjectLogEntity>> = streams?.projectLogs ?: MutableStateFlow(emptyList())
    val calls = mutableListOf<String>()
    override suspend fun log(goalId: String, phaseKey: String, item: String, minutes: Int, date: Long) {
        calls += "log:$goalId:$phaseKey:$item:$minutes:$date"
        logs.value = logs.value + ProjectLogEntity(familyId = "fam", goalId = goalId, phaseKey = phaseKey, item = item, minutes = minutes, date = date)
    }
    override suspend fun toggle(goalId: String, phaseKey: String, item: String, minutes: Int, date: Long) {
        val same = logs.value.filter { it.goalId == goalId && it.item == item && it.date == date }
        if (same.isEmpty()) log(goalId, phaseKey, item, minutes, date) else { calls += "untoggle:$goalId:$item:$date"; logs.value = logs.value - same.toSet() }
    }
    override suspend fun delete(id: String) { calls += "delete:$id"; logs.value = logs.value.filter { it.id != id } }
}

/** [streams] 를 주면 쓰기가 그 파사드의 weekPlans 에도 보여, 실제 앱처럼 쓰기가 읽기에 반영됩니다. 작성자는 [role]. */
class FakeWeekPlanRepository(streams: FakeFamilyDataStreams? = null, var role: String = "STUDENT") : WeekPlanRepository {
    override val plans: MutableStateFlow<List<WeekPlanEntity>> = streams?.weekPlans ?: MutableStateFlow(emptyList())
    val calls = mutableListOf<String>()
    private fun upsert(row: WeekPlanEntity) { plans.value = plans.value.filter { it.id != row.id } + row }
    private fun find(week: LocalDate) = plans.value.firstOrNull { it.weekStart == week.toEpochDay() }
    override suspend fun savePlan(weekStart: LocalDate, goals: List<String>, plannedMinutes: Int) {
        calls += "plan:$weekStart:${goals.joinToString("|")}:$plannedMinutes"
        val base = find(weekStart) ?: WeekPlanEntity(familyId = "fam", weekStart = weekStart.toEpochDay())
        upsert(base.copy(goals = goals.joinToString("\n"), plannedMinutes = plannedMinutes, authorRole = role, approvedAt = null))
    }
    override suspend fun toggleGoal(planId: String, index: Int) {
        calls += "toggle:$planId:$index"
        plans.value.firstOrNull { it.id == planId }?.let { upsert(it.copy(doneMask = it.doneMask xor (1 shl index))) }
    }
    override suspend fun approve(planId: String) {
        calls += "approve:$planId"
        plans.value.firstOrNull { it.id == planId }?.let { upsert(it.copy(approvedAt = 1L)) }
    }
    override suspend fun reflect(weekStart: LocalDate, mood: Int, good: String, hard: String, change: String) {
        calls += "reflect:$weekStart:$mood:$good:$hard:$change"
        val base = find(weekStart) ?: WeekPlanEntity(familyId = "fam", weekStart = weekStart.toEpochDay())
        upsert(base.copy(mood = mood, good = good, hard = hard, change = change, reflectedByRole = role, reflectedAt = 1L))
    }
}

class FakePeerCurriculumRepository : PeerCurriculumRepository {
    override val peerTopics = MutableStateFlow<List<PeerTopicEntity>>(emptyList())
}
