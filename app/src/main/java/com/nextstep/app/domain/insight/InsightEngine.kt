package com.nextstep.app.domain.insight

import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.stats.SubjectMinutes
import com.nextstep.app.domain.stats.SubjectProgress
import com.nextstep.app.domain.stats.SubjectScore
import com.nextstep.app.domain.text.compact
import com.nextstep.app.domain.time.DateUtils

/**
 * 규칙 기반 학습 분석. 성적, 진도, 학습 시간, 일정을 모아 강점/약점/제안을 생성합니다.
 * 순수 함수라 학생/학부모 화면 어디서나 같은 결과를 보여줍니다. 규칙 하나 = 함수 하나.
 */
object InsightEngine {

    fun analyze(
        subjects: List<SubjectEntity>,
        topics: List<TopicEntity>,
        grades: List<GradeEntity>,
        sessions: List<StudySessionEntity>,
        tasks: List<TaskEntity>,
        events: List<EventEntity>,
    ): List<Insight> {
        val scores = StudyStats.subjectScores(grades, subjects)
        val progress = StudyStats.subjectProgress(topics, subjects)
        val weekly = StudyStats.weeklyMinutesBySubject(sessions, subjects)
        val out = buildList {
            addAll(strengthAndWeakness(scores, progress))
            addAll(trends(scores))
            addAll(belowClass(scores))
            addAll(reviewAndPreview(progress))
            addAll(timeBalance(weekly, subjects.size))
            addAll(focusHour(sessions))
            addAll(examsWithoutPrep(events, tasks, subjects))
            addAll(overdue(tasks))
        }
        return out.ifEmpty { listOf(EMPTY) }.sortedBy { ORDER.indexOf(it.kind) }
    }

    /** 1. 강점 / 약점 (성적 기준) */
    private fun strengthAndWeakness(scores: List<SubjectScore>, progress: List<SubjectProgress>): List<Insight> {
        val best = scores.maxByOrNull { it.average }?.takeIf { it.average >= STRONG_AVERAGE }?.let { best ->
            Insight(
                InsightKind.STRENGTH, "${best.subject.name}이(가) 가장 강해요",
                "평균 ${best.average.compact()}점으로 가장 높습니다. 이 과목의 학습 방식을 다른 과목에도 적용해 보세요.",
                best.subject.id,
            )
        }
        val weak = scores.filter { it.average < WEAK_AVERAGE }.sortedBy { it.average }.take(MAX_WEAK).map { weak ->
            val queue = progress.firstOrNull { it.subject.id == weak.subject.id }?.reviewQueue?.firstOrNull()
            Insight(
                InsightKind.WEAKNESS, "${weak.subject.name} 보완이 필요해요",
                "평균 ${weak.average.compact()}점입니다. 최근 배운 단원부터 복습하고 이번 주 학습 시간을 늘려 보세요.",
                weak.subject.id,
                action = InsightAction.CreateTask("${weak.subject.name} ${queue?.title ?: "핵심 단원"} 복습", weak.subject.id, queue?.id, TaskType.REVIEW),
            )
        }
        return listOfNotNull(best) + weak
    }

    /** 2. 추세: 직전 시험보다 크게 오르거나 내린 과목 */
    private fun trends(scores: List<SubjectScore>): List<Insight> = scores.mapNotNull { s ->
        val t = s.trend ?: return@mapNotNull null
        when {
            t <= -SCORE_SWING -> Insight(
                InsightKind.ALERT, "${s.subject.name} 점수가 ${(-t).compact()}점 떨어졌어요",
                "직전 시험 대비 하락했습니다. 틀린 문제 유형을 정리하고 해당 단원을 다시 복습하세요.", s.subject.id,
            )
            t >= SCORE_SWING -> Insight(
                InsightKind.STRENGTH, "${s.subject.name} 점수가 ${t.compact()}점 올랐어요",
                "상승세입니다. 지금 방식을 유지하면서 다음 단원 예습으로 이어가 보세요.", s.subject.id,
            )
            else -> null
        }
    }

    /** 3. 반 평균 대비 */
    private fun belowClass(scores: List<SubjectScore>): List<Insight> = scores.mapNotNull { s ->
        val d = s.vsClass?.takeIf { it <= -SCORE_SWING } ?: return@mapNotNull null
        Insight(
            InsightKind.WEAKNESS, "${s.subject.name} 반 평균보다 ${(-d).compact()}점 낮아요",
            "기본 개념 확인이 우선입니다. 교과서 예제 위주로 복습해 보세요.", s.subject.id,
        )
    }

    /** 4. 복습 밀림 / 예습 제안 (진도 기준) */
    private fun reviewAndPreview(progress: List<SubjectProgress>): List<Insight> = progress.flatMap { p ->
        val reviewBehind = p.classCovered - p.reviewed
        val first = p.reviewQueue.firstOrNull()
        val behind = if (reviewBehind >= REVIEW_BEHIND) Insight(
            InsightKind.ALERT, "${p.subject.name} 복습이 ${reviewBehind}개 단원 밀렸어요",
            "수업은 진행됐지만 복습하지 않은 단원이 쌓였습니다. 오늘 '${first?.title ?: "첫 단원"}'부터 시작해 보세요.",
            p.subject.id,
            action = first?.let { InsightAction.CreateTask("${p.subject.name} ${it.title} 복습", p.subject.id, it.id, TaskType.REVIEW) },
        ) else null
        val next = p.previewQueue.firstOrNull()
        val preview = if (next != null && reviewBehind <= PREVIEW_MAX_BEHIND) Insight(
            InsightKind.SUGGESTION, "${p.subject.name} 다음 단원 예습 추천",
            "곧 배울 '${next.title}'을(를) 미리 훑어보면 수업 이해도가 올라갑니다.",
            p.subject.id,
            action = InsightAction.CreateTask("${p.subject.name} ${next.title} 예습", p.subject.id, next.id, TaskType.PREVIEW),
        ) else null
        listOfNotNull(behind, preview)
    }

    /** 5. 학습 시간 균형: 목표보다 크게 모자란 과목, 한 과목에 몰린 시간 */
    private fun timeBalance(weekly: List<SubjectMinutes>, subjectCount: Int): List<Insight> {
        val short = weekly.mapNotNull { w ->
            val subject = w.subject ?: return@mapNotNull null
            if (w.goalMinutes <= 0 || w.minutes.toFloat() / w.goalMinutes >= SHORT_RATIO) return@mapNotNull null
            Insight(
                InsightKind.SUGGESTION, "${subject.name} 학습 시간이 부족해요",
                "이번 주 ${DateUtils.formatMinutes(w.minutes)} / 목표 ${DateUtils.formatMinutes(w.goalMinutes)}. 남은 요일에 나눠서 채워 보세요.",
                subject.id,
            )
        }
        val weekTotal = weekly.sumOf { it.minutes }
        val top = weekly.filter { it.subject != null }.maxByOrNull { it.minutes }
        val topSubject = top?.subject
        val skewed = if (weekTotal > 0 && top != null && topSubject != null && subjectCount > 1 && top.minutes.toFloat() / weekTotal > SKEW_RATIO) Insight(
            InsightKind.SUGGESTION, "${topSubject.name}에 시간이 몰려 있어요",
            "이번 주 학습 시간의 ${(top.minutes * PERCENT / weekTotal)}%가 한 과목입니다. 다른 과목에도 시간을 배분해 보세요.",
            topSubject.id,
        ) else null
        return short + listOfNotNull(skewed)
    }

    /** 6. 시간대 패턴: 충분히 쌓였을 때만 가장 많이 공부한 시간 */
    private fun focusHour(sessions: List<StudySessionEntity>): List<Insight> {
        val byHour = StudyStats.minutesByHour(sessions)
        if (byHour.sum() < FOCUS_MIN_MINUTES) return emptyList()
        val bestHour = byHour.indices.maxByOrNull { byHour[it] } ?: return emptyList()
        return listOf(
            Insight(
                InsightKind.STRENGTH, "${bestHour}시~${bestHour + 1}시에 가장 집중해요",
                "이 시간대에 학습이 가장 많이 쌓였습니다. 어려운 과목을 이 시간에 배치해 보세요.",
            ),
        )
    }

    /** 7. 다가오는데 준비 할 일이 없는 시험 */
    private fun examsWithoutPrep(events: List<EventEntity>, tasks: List<TaskEntity>, subjects: List<SubjectEntity>): List<Insight> =
        StudyStats.upcomingExams(events, tasks, withinDays = EXAM_WITHIN_DAYS).mapNotNull { exam ->
            val hasPrep = tasks.any { !it.done && it.type == TaskType.EXAM_PREP && (exam.subjectId == null || it.subjectId == exam.subjectId) }
            if (hasPrep) return@mapNotNull null
            val subjectName = subjects.firstOrNull { it.id == exam.subjectId }?.name
            Insight(
                InsightKind.ALERT, "${exam.title} ${DateUtils.dDay(exam.date)}",
                "시험 준비 계획이 없습니다. 지금 준비 항목을 만들어 남은 기간에 나눠 보세요.",
                exam.subjectId,
                action = InsightAction.CreateTask("${subjectName?.let { "$it " } ?: ""}${exam.title} 준비", exam.subjectId, null, TaskType.EXAM_PREP),
            )
        }

    /** 8. 밀린 할 일 */
    private fun overdue(tasks: List<TaskEntity>): List<Insight> {
        val overdue = StudyStats.overdueTasks(tasks)
        val first = overdue.firstOrNull() ?: return emptyList()
        return listOf(
            Insight(
                InsightKind.ALERT, "기한이 지난 할 일이 ${overdue.size}개 있어요",
                "'${first.title}' 등이 미완료입니다. 오늘 처리하거나 기한을 옮겨 주세요.",
            ),
        )
    }

    /** 9. 데이터가 없을 때 안내 */
    private val EMPTY = Insight(
        InsightKind.SUGGESTION, "데이터를 쌓아 보세요",
        "성적, 학습 시간, 단원 진도를 기록하면 강점·약점 분석과 맞춤 제안이 여기 표시됩니다.",
    )

    private val ORDER = listOf(InsightKind.ALERT, InsightKind.WEAKNESS, InsightKind.SUGGESTION, InsightKind.STRENGTH)

    private const val STRONG_AVERAGE = 80
    private const val WEAK_AVERAGE = 70
    private const val MAX_WEAK = 2
    private const val SCORE_SWING = 10
    private const val REVIEW_BEHIND = 3
    private const val PREVIEW_MAX_BEHIND = 1
    private const val SHORT_RATIO = 0.4f
    private const val SKEW_RATIO = 0.6f
    private const val PERCENT = 100
    private const val FOCUS_MIN_MINUTES = 180
    private const val EXAM_WITHIN_DAYS = 14
}
