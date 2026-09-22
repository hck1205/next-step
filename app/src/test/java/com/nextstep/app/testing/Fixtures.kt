package com.nextstep.app.testing

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
import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.LocalTime

/** 테스트 데이터 빌더. 의미 있는 값만 넘기고 나머지는 기본값. */
object Fixtures {
    const val FAMILY = "fam"
    val math = subject("math", "수학")
    val english = subject("eng", "영어")

    fun subject(id: String, name: String, goal: Int = 180) = SubjectEntity(id = id, familyId = FAMILY, name = name, color = 0xFF3B82F6, weeklyGoalMinutes = goal)

    fun topic(subjectId: String, title: String, order: Int, covered: Boolean = false, status: TopicStatus = TopicStatus.NOT_STARTED, confidence: Int = 0, id: String = "t-$subjectId-$order") =
        TopicEntity(id = id, familyId = FAMILY, subjectId = subjectId, title = title, orderIndex = order, classCovered = covered, status = status, confidence = confidence)

    /** [count]개 단원 중 앞 [covered]개는 수업 완료, 그중 앞 [reviewed]개는 복습 완료. */
    fun topics(subjectId: String, count: Int, covered: Int, reviewed: Int = 0): List<TopicEntity> = (0 until count).map { i ->
        topic(subjectId, "단원 $i", i, covered = i < covered, status = when { i < reviewed -> TopicStatus.REVIEWED; i < covered -> TopicStatus.IN_CLASS; else -> TopicStatus.NOT_STARTED })
    }

    fun grade(subjectId: String, score: Double, day: Long, classAvg: Double? = null, id: String = "g-$subjectId-$day") =
        GradeEntity(id = id, familyId = FAMILY, subjectId = subjectId, title = "시험", score = score, date = day, classAverage = classAvg)

    fun session(subjectId: String?, date: LocalDate, start: LocalTime, minutes: Int, id: String = "s-${date.toEpochDay()}-${start.toSecondOfDay()}"): StudySessionEntity {
        val startMs = DateUtils.toMillis(date, start)
        return StudySessionEntity(id = id, familyId = FAMILY, subjectId = subjectId, startAt = startMs, endAt = startMs + minutes * 60_000L, durationMinutes = minutes)
    }

    fun task(title: String, due: LocalDate, subjectId: String? = null, done: Boolean = false, type: TaskType = TaskType.HOMEWORK, by: String = "STUDENT", id: String = "task-$title") =
        TaskEntity(id = id, familyId = FAMILY, subjectId = subjectId, title = title, type = type, dueDate = due.toEpochDay(), done = done, createdByRole = by)

    fun event(title: String, date: LocalDate, start: LocalTime, end: LocalTime, type: EventType = EventType.CLASS, subjectId: String? = null, weekly: Boolean = false, id: String = "ev-$title") =
        EventEntity(id = id, familyId = FAMILY, subjectId = subjectId, title = title, type = type, startAt = DateUtils.toMillis(date, start), endAt = DateUtils.toMillis(date, end), repeatWeekly = weekly)

    fun note(text: String, role: Role = Role.PARENT, author: String = "엄마", id: String = "n-$text") =
        NoteEntity(id = id, familyId = FAMILY, authorRole = role.name, authorName = author, text = text)

    fun member(role: Role, name: String, id: String = "m-$name", subjectIds: String = "", mentorEnabled: Boolean = role == Role.MENTOR, gradeYear: Int = 0, birthDate: LocalDate? = null) =
        MemberEntity(id = id, familyId = FAMILY, role = role.name, name = name, subjectIds = subjectIds, mentorEnabled = mentorEnabled, gradeYear = gradeYear, birthDate = birthDate?.toEpochDay())

    fun goal(title: String, trackId: String? = null, id: String = "g-${trackId ?: title}", status: GoalStatus = GoalStatus.ACTIVE, area: String = "MATH") =
        GoalEntity(id = id, familyId = FAMILY, trackId = trackId, title = title, area = area, status = status)

    fun step(goalId: String, periodKey: String, title: String, id: String = "st-$goalId-$periodKey", order: Int = 0, status: MilestoneStatus = MilestoneStatus.UPCOMING, taskId: String? = null) =
        GoalStepEntity(id = id, familyId = FAMILY, goalId = goalId, periodKey = periodKey, orderIndex = order, title = title, status = status, taskId = taskId)

    fun journeyItem(templateId: String?, status: MilestoneStatus = MilestoneStatus.UPCOMING, due: LocalDate = LocalDate.of(2027, 1, 1), title: String = "", id: String = "j-${templateId ?: title}", leadMonths: Int = 1, note: String = "", category: String = "ADMIN") =
        JourneyItemEntity(id = id, familyId = FAMILY, templateId = templateId, title = title, category = category, dueDate = due.toEpochDay(), leadMonths = leadMonths, status = status, note = note)

    fun roadmap(title: String, subjectId: String? = null, status: RoadmapStatus = RoadmapStatus.PLANNED, target: LocalDate? = null, id: String = "r-$title", contentId: String? = null) =
        RoadmapItemEntity(id = id, familyId = FAMILY, subjectId = subjectId, title = title, status = status, targetDate = target?.toEpochDay(), contentId = contentId)

    fun content(title: String, subjectKey: String = "", type: ContentType = ContentType.CONCEPT, keywords: String = "", id: String = "c-$title", watched: Boolean = false) =
        ContentEntity(id = id, familyId = FAMILY, url = "https://youtu.be/$id", videoId = id, title = title, subjectKey = subjectKey, contentType = type, keywords = keywords, watched = watched)
}
