package com.nextstep.app.domain.task

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.curriculum.CurriculumUnit
import com.nextstep.app.domain.growth.StudyKind
import com.nextstep.app.domain.growth.StudyKindType
import com.nextstep.app.domain.insight.InsightAction
import com.nextstep.app.domain.taskboard.TaskSuggestion
import com.nextstep.app.domain.year.YearArea
import com.nextstep.app.domain.year.YearTask
import java.time.LocalDate

/**
 * 새 할 일의 모양(제목·종류·마감·메모)을 한 곳에서 정합니다. 화면마다 제목 짓는 법이 달라지지 않게,
 * ViewModel 은 여기서 받은 할 일을 저장만 합니다. 가족 id 는 저장소가 채웁니다.
 */
object TaskDrafts {

    /** 사람이 직접 쓴 할 일(기록하기, 학부모·멘토의 할 일 주기). */
    fun written(title: String, subjectId: String?, type: TaskType, due: LocalDate, byRole: String): TaskEntity =
        TaskEntity(familyId = "", subjectId = subjectId, title = title, type = type, dueDate = due.toEpochDay(), createdByRole = byRole)

    /** 단원 하나의 예습·복습·숙제. 제목은 "과목 단원 종류"(예: "수학 분수 복습"). */
    fun forTopic(subject: SubjectEntity, topic: TopicEntity, type: TaskType, due: LocalDate, byRole: String): TaskEntity = TaskEntity(
        familyId = "", subjectId = subject.id, topicId = topic.id, title = "${subject.name} ${topic.title} ${type.label}",
        type = type, dueDate = due.toEpochDay(), createdByRole = byRole,
    )

    /** 단원 할 일의 마감: 학생이 스스로 만들면 오늘, 어른이 주면 하루 여유를 두고 내일. */
    fun topicDue(byRole: String, today: LocalDate): LocalDate = if (byRole == Role.STUDENT.name) today else today.plusDays(1)

    /** 올해의 공부 한 가지를 그 분량의 오늘 할 일로. 시험 준비는 시험 준비 종류로. */
    fun forStudyKind(kind: StudyKind, today: LocalDate): TaskEntity = TaskEntity(
        familyId = "", title = "${kind.name} ${kind.minutes}분",
        type = if (kind.type == StudyKindType.TEST_PREP) TaskType.EXAM_PREP else TaskType.HOMEWORK,
        dueDate = today.toEpochDay(), createdByRole = Role.STUDENT.name,
    )

    /** 올해 할 일 한 줄을 오늘 할 일로. 하는 법은 메모로 따라갑니다. */
    fun forYearTask(task: YearTask, today: LocalDate): TaskEntity = TaskEntity(
        familyId = "", title = task.title, type = if (task.area == YearArea.EXAM) TaskType.EXAM_PREP else TaskType.HOMEWORK,
        dueDate = today.toEpochDay(), createdByRole = Role.STUDENT.name, note = task.how,
    )

    /** 분석 제안에서 만든 할 일. 제안을 본 다음 날까지. */
    fun forInsight(action: InsightAction.CreateTask, byRole: String, today: LocalDate): TaskEntity = TaskEntity(
        familyId = "", subjectId = action.subjectId, topicId = action.topicId, title = action.title, type = action.type,
        dueDate = today.plusDays(1).toEpochDay(), createdByRole = byRole,
    )

    /** 시스템 추천을 받아들인 할 일. 목표에 넣으면 그 목표의 세부 할 일이 되고, 메모에 목표와 추천 근거가 남습니다. */
    fun forSuggestion(s: TaskSuggestion, goal: GoalEntity?, byRole: String): TaskEntity = TaskEntity(
        familyId = "", subjectId = s.subjectId, topicId = s.topicId, title = s.title, type = s.type, dueDate = s.due.toEpochDay(),
        createdByRole = byRole, note = listOfNotNull(goal?.title, s.source.label).joinToString(" · "), goalId = goal?.id,
    )

    /** 이번 학기 커리큘럼 단원 하나. 제목은 "과목 · 단원". */
    fun forCurriculum(unit: CurriculumUnit, subjectId: String?, type: TaskType, due: LocalDate, byRole: String): TaskEntity = TaskEntity(
        familyId = "", subjectId = subjectId, title = "${unit.subject} · ${unit.title}", type = type, dueDate = due.toEpochDay(),
        createdByRole = byRole, note = CURRICULUM_NOTE,
    )

    private const val CURRICULUM_NOTE = "이번 학기 커리큘럼"
}
