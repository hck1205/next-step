package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TopicStatus
import java.time.LocalDate
import com.nextstep.app.domain.time.DateUtils

/** 화면과 인사이트 엔진이 공유하는 순수 계산 함수 모음. */
object StudyStats {

    fun subjectScores(grades: List<GradeEntity>, subjects: List<SubjectEntity>): List<SubjectScore> =
        subjects.mapNotNull { subject ->
            val list = grades.filter { it.subjectId == subject.id }.sortedBy { it.date }
            if (list.isEmpty()) return@mapNotNull null
            val latest = list.last()
            val prev = list.dropLast(1).lastOrNull()
            SubjectScore(
                subject = subject,
                average = list.map { it.percent }.average(),
                latest = latest.percent,
                trend = prev?.let { latest.percent - it.percent },
                count = list.size,
                vsClass = latest.classAverage?.let { latest.score - it },
            )
        }

    fun subjectProgress(topics: List<TopicEntity>, subjects: List<SubjectEntity>, queueSize: Int = 3): List<SubjectProgress> =
        subjects.map { subject ->
            val list = topics.filter { it.subjectId == subject.id }.sortedBy { it.orderIndex }
            SubjectProgress(
                subject = subject,
                total = list.size,
                classCovered = list.count { it.classCovered },
                reviewed = list.count { it.status.order >= TopicStatus.REVIEWED.order },
                previewed = list.count { it.status.order >= TopicStatus.PREVIEWED.order },
                previewQueue = list.filter { !it.classCovered && it.status.order < TopicStatus.PREVIEWED.order }.take(queueSize),
                reviewQueue = list.filter { it.classCovered && it.status.order < TopicStatus.REVIEWED.order }.take(queueSize),
            )
        }

    fun minutesBetween(sessions: List<StudySessionEntity>, from: LocalDate, toExclusive: LocalDate): Int {
        val fromMs = DateUtils.startOfDayMillis(from)
        val toMs = DateUtils.startOfDayMillis(toExclusive)
        return sessions.filter { it.startAt in fromMs until toMs }.sumOf { it.durationMinutes }
    }

    fun todayMinutes(sessions: List<StudySessionEntity>): Int {
        val today = DateUtils.today()
        return minutesBetween(sessions, today, today.plusDays(1))
    }

    fun weekMinutes(sessions: List<StudySessionEntity>): Int {
        val start = DateUtils.weekStart()
        return minutesBetween(sessions, start, start.plusWeeks(1))
    }

    /** 최근 n일 일별 학습 시간(오래된 날부터). */
    fun dailyMinutes(sessions: List<StudySessionEntity>, days: Int = 7): List<DayMinutes> {
        val today = DateUtils.today()
        return (days - 1 downTo 0).map { back ->
            val d = today.minusDays(back.toLong())
            DayMinutes(d, minutesBetween(sessions, d, d.plusDays(1)))
        }
    }

    /** 이번 주 과목별 학습 시간과 목표. */
    fun weeklyMinutesBySubject(sessions: List<StudySessionEntity>, subjects: List<SubjectEntity>): List<SubjectMinutes> {
        val start = DateUtils.weekStart()
        val fromMs = DateUtils.startOfDayMillis(start)
        val toMs = DateUtils.startOfDayMillis(start.plusWeeks(1))
        val weekSessions = sessions.filter { it.startAt in fromMs until toMs }
        val bySubject = subjects.map { s ->
            SubjectMinutes(s, weekSessions.filter { it.subjectId == s.id }.sumOf { it.durationMinutes }, s.weeklyGoalMinutes)
        }
        val other = weekSessions.filter { sess -> sess.subjectId == null || subjects.none { it.id == sess.subjectId } }
            .sumOf { it.durationMinutes }
        return if (other > 0) bySubject + SubjectMinutes(null, other, 0) else bySubject
    }

    /** 시간대별(0~23시) 학습 분 합계. 집중 시간대 분석용. */
    fun minutesByHour(sessions: List<StudySessionEntity>): IntArray {
        val hours = IntArray(24)
        sessions.forEach { s ->
            val hour = DateUtils.toLocalDateTime(s.startAt).hour
            hours[hour] += s.durationMinutes
        }
        return hours
    }

    /** 특정 날짜의 일정. 주간 반복 일정은 요일이 같으면 포함합니다. */
    fun eventsOn(date: LocalDate, events: List<EventEntity>): List<EventOccurrence> {
        val dayStart = DateUtils.startOfDayMillis(date)
        val dayEnd = DateUtils.startOfDayMillis(date.plusDays(1))
        return events.mapNotNull { e ->
            if (e.repeatWeekly) {
                val first = DateUtils.toLocalDateTime(e.startAt)
                if (first.dayOfWeek != date.dayOfWeek || first.toLocalDate().isAfter(date)) return@mapNotNull null
                val duration = e.endAt - e.startAt
                val start = DateUtils.toMillis(date, first.toLocalTime())
                EventOccurrence(e, start, start + duration)
            } else {
                if (e.startAt in dayStart until dayEnd) EventOccurrence(e, e.startAt, e.endAt) else null
            }
        }.sortedBy { it.startAt }
    }

    /** 오늘 이후 첫 시험 일정. */
    fun upcomingExams(events: List<EventEntity>, tasks: List<TaskEntity>, withinDays: Int = 30): List<UpcomingExam> {
        val today = DateUtils.today()
        val result = mutableListOf<UpcomingExam>()
        (0..withinDays).forEach { offset ->
            val d = today.plusDays(offset.toLong())
            eventsOn(d, events).filter { it.event.type == com.nextstep.app.data.model.EventType.EXAM }.forEach {
                result += UpcomingExam(it.event.title, it.event.subjectId, d)
            }
        }
        return result.sortedBy { it.date }
    }

    fun pendingTasks(tasks: List<TaskEntity>, onOrBefore: LocalDate = DateUtils.today()): List<TaskEntity> =
        tasks.filter { !it.done && it.dueDate <= onOrBefore.toEpochDay() }.sortedBy { it.dueDate }

    /** 오늘(또는 어제)까지 연속으로 학습한 일수. */
    fun studyStreak(sessions: List<StudySessionEntity>): Int {
        val days = sessions.map { DateUtils.toLocalDate(it.startAt) }.toSet()
        var day = DateUtils.today()
        if (day !in days) day = day.minusDays(1)
        var streak = 0
        while (day in days) { streak++; day = day.minusDays(1) }
        return streak
    }

    fun overdueTasks(tasks: List<TaskEntity>): List<TaskEntity> =
        tasks.filter { !it.done && it.dueDate < DateUtils.today().toEpochDay() }
}
