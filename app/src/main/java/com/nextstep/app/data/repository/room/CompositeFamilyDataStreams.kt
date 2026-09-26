package com.nextstep.app.data.repository.room

import com.nextstep.app.data.repository.ActivityRepository
import com.nextstep.app.data.repository.ContentRepository
import com.nextstep.app.data.repository.EventRepository
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.GoalRepository
import com.nextstep.app.data.repository.GradeRepository
import com.nextstep.app.data.repository.GrowthRepository
import com.nextstep.app.data.repository.JourneyRepository
import com.nextstep.app.data.repository.MemberRepository
import com.nextstep.app.data.repository.OnboardingRepository
import com.nextstep.app.data.repository.ProjectRepository
import com.nextstep.app.data.repository.RoadmapRepository
import com.nextstep.app.data.repository.StudySessionRepository
import com.nextstep.app.data.repository.SubjectRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.data.repository.TopicRepository

/** 개별 저장소의 읽기 스트림을 한 곳에 모읍니다. 새 스트림은 여기와 인터페이스에만 추가합니다. */
class CompositeFamilyDataStreams(
    onboarding: OnboardingRepository,
    subjects: SubjectRepository,
    topics: TopicRepository,
    tasks: TaskRepository,
    events: EventRepository,
    grades: GradeRepository,
    sessions: StudySessionRepository,
    members: MemberRepository,
    roadmap: RoadmapRepository,
    contents: ContentRepository,
    journey: JourneyRepository,
    goals: GoalRepository,
    activities: ActivityRepository,
    growth: GrowthRepository,
    projects: ProjectRepository,
) : FamilyDataStreams {
    override val profile = onboarding.profile
    override val syncStatus = onboarding.syncStatus
    override val subjects = subjects.subjects
    override val topics = topics.topics
    override val tasks = tasks.tasks
    override val events = events.events
    override val grades = grades.grades
    override val sessions = sessions.sessions
    override val members = members.members
    override val myMember = members.myMember
    override val roadmap = roadmap.roadmap
    override val contents = contents.contents
    override val runningTimer = sessions.runningTimer
    override val journeyItems = journey.items
    override val goals = goals.goals
    override val goalSteps = goals.steps
    override val activities = activities.activities
    override val growthRecords = growth.records
    override val observations = growth.observations
    override val projectLogs = projects.logs
}
