package com.nextstep.app.domain.feedback

import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/**
 * 주간 피드백: 쌓인 기록에서 이번 주(최근 7일)와 지난주(그 전 7일)를 견주어 사실을 한 벌 찾습니다. 순수 함수이고, 규칙 하나 = 함수 하나.
 * 같은 사실을 학생·학부모·멘토가 각자의 말로 듣습니다([FeedbackVoice]). 비교는 아이 자신의 지난주와만 합니다(또래·평균 없음).
 */
object FeedbackEngine {

    fun findings(
        subjects: List<SubjectEntity>,
        topics: List<TopicEntity>,
        grades: List<GradeEntity>,
        sessions: List<StudySessionEntity>,
        tasks: List<TaskEntity>,
        today: LocalDate,
    ): List<Finding> {
        val liveSessions = sessions.filter { !it.deleted }
        val liveTasks = tasks.filter { !it.deleted }
        return buildList {
            addAll(studyDays(liveSessions, today))
            addAll(taskFlow(liveTasks, today))
            addAll(selfMade(liveTasks, today))
            addAll(scores(grades.filter { !it.deleted }, subjects, today))
            addAll(subjectGap(liveSessions, subjects, today))
            addAll(reviewBacklog(topics.filter { !it.deleted }, subjects))
        }
    }

    /**
     * 보는 사람에게 보여 줄 것: 잘한 것 하나 먼저, 챙길 것은 그 뒤로 합쳐 [MAX_LINES] 개까지.
     * 멘토는 가족의 일([FeedbackKind.familyOnly])을 빼고, 과목이 있는 사실은 담당 과목([mentorSubjects], null 이면 전 과목) 것만.
     */
    fun forAudience(findings: List<Finding>, audience: FeedbackAudience, mentorSubjects: Set<String>? = null): List<Finding> {
        val seen = if (audience != FeedbackAudience.MENTOR) findings else findings.filter {
            !it.kind.familyOnly && (it.subjectId == null || mentorSubjects == null || it.subjectId in mentorSubjects)
        }
        val good = seen.filter { it.kind.good }.take(1)
        return good + seen.filter { !it.kind.good }.take(MAX_LINES - good.size)
    }

    /** 1. 공부한 날: 늘었거나, 꾸준하거나, 줄었거나. */
    private fun studyDays(sessions: List<StudySessionEntity>, today: LocalDate): List<Finding> {
        val days = sessions.map { DateUtils.toLocalDate(it.startAt) }.toSet()
        val now = days.count { it in thisWeek(today) }
        val before = days.count { it in lastWeek(today) }
        return listOfNotNull(
            when {
                before - now >= DROP_DAYS -> Finding(FeedbackKind.STUDY_DAYS_DOWN, now, before)
                now - before >= RISE_DAYS -> Finding(FeedbackKind.STUDY_DAYS_UP, now, before)
                now >= STEADY_DAYS -> Finding(FeedbackKind.STUDY_STEADY, now, before)
                else -> null
            },
        )
    }

    /** 2. 이번 주 마감이던 할 일을 거의 다 끝냈는지, 밀린 할 일이 쌓였는지. */
    private fun taskFlow(tasks: List<TaskEntity>, today: LocalDate): List<Finding> {
        val due = tasks.filter { DateUtils.fromEpochDay(it.dueDate) in thisWeek(today) }
        val done = due.count { it.done }
        val overdue = tasks.count { !it.done && it.dueDate < today.toEpochDay() }
        return listOfNotNull(
            Finding(FeedbackKind.TASKS_WELL, done, due.size).takeIf { due.size >= MIN_TASKS && done * PERCENT >= due.size * WELL_PERCENT },
            Finding(FeedbackKind.TASKS_OVERDUE, overdue).takeIf { overdue >= OVERDUE_ALERT },
        )
    }

    /** 3. 끝낸 일 가운데 스스로 정한 몫이 지난주보다 늘었는지(가족의 일). */
    private fun selfMade(tasks: List<TaskEntity>, today: LocalDate): List<Finding> {
        val done = tasks.mapNotNull { t -> t.doneAt?.takeIf { t.done }?.let { DateUtils.toLocalDate(it) to t } }
        val now = done.filter { it.first in thisWeek(today) }.map { it.second }
        val before = done.filter { it.first in lastWeek(today) }.map { it.second }
        val nowPercent = percent(now.count { it.isStudentMade }, now.size)
        val beforePercent = percent(before.count { it.isStudentMade }, before.size)
        val rose = now.count { it.isStudentMade } >= MIN_SELF_TASKS && nowPercent - beforePercent >= SELF_STEP_PERCENT
        return listOfNotNull(Finding(FeedbackKind.SELF_MADE_UP, nowPercent, beforePercent).takeIf { rose })
    }

    /** 4. 과목마다 최근 두 번의 점수(최근 것이 2주 안)가 크게 오르거나 내렸는지. */
    private fun scores(grades: List<GradeEntity>, subjects: List<SubjectEntity>, today: LocalDate): List<Finding> =
        subjects.mapNotNull { s ->
            val two = grades.filter { it.subjectId == s.id }.sortedBy { it.date }.takeLast(2)
            if (two.size < 2 || two[1].date < today.minusDays(RECENT_SCORE_DAYS).toEpochDay()) return@mapNotNull null
            val (before, now) = two.map { it.percent.toInt() }
            when {
                now - before >= SCORE_STEP -> Finding(FeedbackKind.SCORE_UP, now, before, s.id, s.name)
                before - now >= SCORE_STEP -> Finding(FeedbackKind.SCORE_DOWN, now, before, s.id, s.name)
                else -> null
            }
        }.take(MAX_SCORES)

    /** 5. 그 전 3주엔 하던 과목을 이번 주에 한 번도 안 봤는지. 가장 많이 하던 과목 하나. */
    private fun subjectGap(sessions: List<StudySessionEntity>, subjects: List<SubjectEntity>, today: LocalDate): List<Finding> {
        val earlier = today.minusDays(GAP_LOOKBACK_DAYS)..today.minusDays(WEEK_DAYS)
        val gap = subjects.map { s -> s to sessions.filter { it.subjectId == s.id } }
            .filter { (_, list) -> list.none { DateUtils.toLocalDate(it.startAt) in thisWeek(today) } }
            .map { (s, list) -> Triple(s, list.filter { DateUtils.toLocalDate(it.startAt) in earlier }.sumOf { it.durationMinutes }, list) }
            .filter { it.second >= GAP_BEFORE_MINUTES }
            .maxByOrNull { it.second } ?: return emptyList()
        val last = gap.third.maxOf { DateUtils.toLocalDate(it.startAt) }
        return listOf(Finding(FeedbackKind.SUBJECT_GAP, (today.toEpochDay() - last.toEpochDay()).toInt(), subjectId = gap.first.id, subjectName = gap.first.name))
    }

    /** 6. 수업한 단원 가운데 복습하지 않은 것이 쌓인 과목(가장 많은 하나). */
    private fun reviewBacklog(topics: List<TopicEntity>, subjects: List<SubjectEntity>): List<Finding> {
        val worst = subjects.map { s -> s to topics.count { it.subjectId == s.id && it.classCovered && it.status < TopicStatus.REVIEWED } }
            .filter { it.second >= BACKLOG_TOPICS }
            .maxByOrNull { it.second } ?: return emptyList()
        return listOf(Finding(FeedbackKind.REVIEW_BACKLOG, worst.second, subjectId = worst.first.id, subjectName = worst.first.name))
    }

    private fun thisWeek(today: LocalDate) = today.minusDays(WEEK_DAYS - 1)..today
    private fun lastWeek(today: LocalDate) = today.minusDays(2 * WEEK_DAYS - 1)..today.minusDays(WEEK_DAYS)
    private fun percent(part: Int, whole: Int): Int = if (whole == 0) 0 else part * PERCENT / whole

    const val MAX_LINES = 3
    private const val WEEK_DAYS = 7L
    private const val PERCENT = 100
    private const val DROP_DAYS = 2
    private const val RISE_DAYS = 2
    private const val STEADY_DAYS = 4
    private const val MIN_TASKS = 3
    private const val WELL_PERCENT = 80
    private const val OVERDUE_ALERT = 3
    private const val MIN_SELF_TASKS = 2
    private const val SELF_STEP_PERCENT = 10
    private const val RECENT_SCORE_DAYS = 14L
    private const val SCORE_STEP = 5
    private const val MAX_SCORES = 2
    private const val GAP_LOOKBACK_DAYS = 28L
    private const val GAP_BEFORE_MINUTES = 60
    private const val BACKLOG_TOPICS = 3
}
