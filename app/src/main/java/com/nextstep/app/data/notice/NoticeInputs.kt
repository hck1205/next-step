package com.nextstep.app.data.notice

import com.nextstep.app.data.model.Role
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.family.StudentContext
import com.nextstep.app.domain.familycalendar.FamilyCalendar
import com.nextstep.app.domain.journey.JourneyPlanner
import com.nextstep.app.domain.mission.MissionPlanner
import com.nextstep.app.domain.notice.NoticeInput
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import kotlinx.coroutines.flow.first

/** 알림 재료를 지금 기록에서 한 번 읽어 모읍니다. 보는 범위는 [streams](멘토 범위·프로젝트 범위가 이미 적용된 것) 그대로입니다. */
object NoticeInputs {
    suspend fun of(streams: FamilyDataStreams, today: LocalDate): NoticeInput {
        val profile = streams.profile.first()
        val caps = Capabilities.of(profile.role ?: Role.STUDENT, streams.myMember.first())
        val family = streams.familyEvents.first()
        val tasks = streams.tasks.first()
        val nextMonday = DateUtils.weekStart(today).plusWeeks(1)
        val birth = StudentContext.of(streams.members.first(), today).birthDate
        return NoticeInput(
            family = caps.isFamily, isStudent = caps.isStudent,
            familyAhead = FamilyCalendar.ahead(family, today),
            nextWeek = (0L until DAYS_IN_WEEK).flatMap { FamilyCalendar.on(family, nextMonday.plusDays(it)) }.filter { it.isFirstDay }.distinctBy { it.event.id },
            exams = StudyStats.upcomingExams(streams.events.first(), tasks),
            missions = MissionPlanner.focus(streams.goals.first(), streams.goalSteps.first(), today),
            journey = if (caps.isFamily) JourneyPlanner.actionable(JourneyPlanner.build(birth, streams.journeyItems.first(), today), today) else emptyList(),
            dueToday = tasks.count { !it.deleted && !it.done && it.dueDate == today.toEpochDay() },
            overdue = StudyStats.overdueTasks(tasks).size,
        )
    }

    private const val DAYS_IN_WEEK = 7L
}
