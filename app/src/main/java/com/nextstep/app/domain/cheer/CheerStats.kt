package com.nextstep.app.domain.cheer

import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/** 오늘 기준으로 [CheerSnapshot] 을 만듭니다. */
object CheerStats {
    fun snapshot(sessions: List<StudySessionEntity>, tasks: List<TaskEntity>, topics: List<TopicEntity>, subjects: List<SubjectEntity>, today: LocalDate): CheerSnapshot {
        val todayStart = DateUtils.startOfDayMillis(today)
        val todayMinutes = StudyStats.todayMinutes(sessions)
        val minutesBySubject = sessions.filter { it.startAt >= todayStart }.groupBy { it.subjectId }.mapValues { (_, s) -> s.sumOf { it.durationMinutes } }
        val top = subjects.maxByOrNull { minutesBySubject[it.id] ?: 0 }?.takeIf { (minutesBySubject[it.id] ?: 0) > 0 }
        return CheerSnapshot(
            todayMinutes = todayMinutes,
            streak = StudyStats.studyStreak(sessions),
            doneToday = tasks.count { it.done && it.updatedAt >= todayStart },
            reviewedToday = topics.count { it.updatedAt >= todayStart && it.status.order >= TopicStatus.REVIEWED.order },
            topSubjectName = top?.name,
        )
    }
}
