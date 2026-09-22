package com.nextstep.app.domain

import com.nextstep.app.data.local.EventEntity
import com.nextstep.app.data.local.RoadmapItemEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.local.TaskEntity
import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.TaskType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class PlanOptions(
    val days: Int = 7,
    val startTime: LocalTime = LocalTime.of(19, 0),
    val sessionMinutes: Int = 50,
    val breakMinutes: Int = 10,
    val sessionsPerDay: Int = 2,
    val includeWeekend: Boolean = true,
)

/** 계획에 넣을 학습 항목 하나. 복습/예습 단원이나 로드맵 항목에서 만들어집니다. */
data class PlanItem(
    val subject: SubjectEntity?,
    val title: String,
    val taskType: TaskType,
    val topicId: String? = null,
    val roadmapId: String? = null,
)

data class StudyPlan(val events: List<EventEntity>, val tasks: List<TaskEntity>) {
    val isEmpty: Boolean get() = events.isEmpty()
}

/**
 * 커리큘럼 스케줄링: 밀린 복습 → 멘토 로드맵 진행 항목 → 다음 예습 순서로 큐를 만들고,
 * 기존 일정과 겹치지 않는 저녁 시간대에 자습 일정과 할 일로 배치합니다.
 */
object StudyPlanner {

    fun buildQueue(progress: List<SubjectProgress>, roadmap: List<RoadmapItemEntity>, subjects: List<SubjectEntity>): List<PlanItem> {
        val queue = mutableListOf<PlanItem>()
        // 1. 복습: 과목을 번갈아 가며 하나씩 (한 과목에 몰리지 않게)
        val reviewLists = progress.map { p -> p.reviewQueue.map { t -> PlanItem(p.subject, "복습: ${p.subject.name} ${t.title}", TaskType.REVIEW, topicId = t.id) } }
        queue += interleave(reviewLists)
        // 2. 로드맵: 진행 중 → 예정 순, 목표일 빠른 순
        queue += roadmap.filter { it.status != RoadmapStatus.DONE }
            .sortedWith(compareBy<RoadmapItemEntity> { it.status != RoadmapStatus.IN_PROGRESS }.thenBy { it.targetDate ?: Long.MAX_VALUE }.thenBy { it.orderIndex })
            .map { r -> PlanItem(subjects.firstOrNull { it.id == r.subjectId }, "로드맵: ${r.title}", TaskType.OTHER, roadmapId = r.id) }
        // 3. 예습
        val previewLists = progress.map { p -> p.previewQueue.take(1).map { t -> PlanItem(p.subject, "예습: ${p.subject.name} ${t.title}", TaskType.PREVIEW, topicId = t.id) } }
        queue += interleave(previewLists)
        return queue
    }

    fun generate(
        queue: List<PlanItem>,
        existingEvents: List<EventEntity>,
        options: PlanOptions = PlanOptions(),
        from: LocalDate = DateUtils.today().plusDays(1),
        createdByRole: String = "STUDENT",
    ): StudyPlan {
        if (queue.isEmpty()) return StudyPlan(emptyList(), emptyList())
        val events = mutableListOf<EventEntity>()
        val tasks = mutableListOf<TaskEntity>()
        var index = 0
        for (offset in 0 until options.days) {
            if (index >= queue.size) break
            val day = from.plusDays(offset.toLong())
            if (!options.includeWeekend && (day.dayOfWeek == DayOfWeek.SATURDAY || day.dayOfWeek == DayOfWeek.SUNDAY)) continue
            val busy = StudyStats.eventsOn(day, existingEvents)
            for (slot in 0 until options.sessionsPerDay) {
                if (index >= queue.size) break
                val start = options.startTime.plusMinutes(((options.sessionMinutes + options.breakMinutes) * slot).toLong())
                val end = start.plusMinutes(options.sessionMinutes.toLong())
                if (end.isBefore(start)) break // 자정을 넘김
                val startMs = DateUtils.toMillis(day, start)
                val endMs = DateUtils.toMillis(day, end)
                val overlaps = busy.any { it.startAt < endMs && it.endAt > startMs }
                if (overlaps) continue
                val item = queue[index++]
                events += EventEntity(
                    familyId = "", subjectId = item.subject?.id, title = item.title, type = EventType.STUDY,
                    startAt = startMs, endAt = endMs, memo = "학습 계획 자동 생성",
                )
                tasks += TaskEntity(
                    familyId = "", subjectId = item.subject?.id, topicId = item.topicId, title = item.title.substringAfter(": "),
                    type = item.taskType, dueDate = day.toEpochDay(), createdByRole = createdByRole,
                    note = if (item.roadmapId != null) "roadmap:${item.roadmapId}" else "",
                )
            }
        }
        return StudyPlan(events, tasks)
    }

    private fun <T> interleave(lists: List<List<T>>): List<T> {
        val out = mutableListOf<T>()
        val max = lists.maxOfOrNull { it.size } ?: 0
        for (i in 0 until max) lists.forEach { l -> l.getOrNull(i)?.let { out += it } }
        return out
    }
}
