package com.nextstep.app.domain.insight

import com.nextstep.app.domain.text.compact
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.time.DateUtils

/**
 * 규칙 기반 학습 분석. 성적, 진도, 학습 시간, 일정을 모아 강점/약점/제안을 생성합니다.
 * 순수 함수라 학생/학부모 화면 어디서나 같은 결과를 보여줍니다.
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
        val out = mutableListOf<Insight>()
        val scores = StudyStats.subjectScores(grades, subjects)
        val progress = StudyStats.subjectProgress(topics, subjects)
        val weekly = StudyStats.weeklyMinutesBySubject(sessions, subjects)

        // 1. 강점 / 약점 (성적 기준)
        scores.maxByOrNull { it.average }?.let { best ->
            if (best.average >= 80) {
                out += Insight(
                    InsightKind.STRENGTH, "${best.subject.name}이(가) 가장 강해요",
                    "평균 ${best.average.compact()}점으로 가장 높습니다. 이 과목의 학습 방식을 다른 과목에도 적용해 보세요.",
                    best.subject.id,
                )
            }
        }
        scores.filter { it.average < 70 }.sortedBy { it.average }.take(2).forEach { weak ->
            val queue = progress.firstOrNull { it.subject.id == weak.subject.id }?.reviewQueue?.firstOrNull()
            out += Insight(
                InsightKind.WEAKNESS, "${weak.subject.name} 보완이 필요해요",
                "평균 ${weak.average.compact()}점입니다. 최근 배운 단원부터 복습하고 이번 주 학습 시간을 늘려 보세요.",
                weak.subject.id,
                action = InsightAction.CreateTask("${weak.subject.name} ${queue?.title ?: "핵심 단원"} 복습", weak.subject.id, queue?.id, TaskType.REVIEW),
            )
        }

        // 2. 추세
        scores.forEach { s ->
            val t = s.trend ?: return@forEach
            when {
                t <= -10 -> out += Insight(
                    InsightKind.ALERT, "${s.subject.name} 점수가 ${(-t).compact()}점 떨어졌어요",
                    "직전 시험 대비 하락했습니다. 틀린 문제 유형을 정리하고 해당 단원을 다시 복습하세요.", s.subject.id,
                )
                t >= 10 -> out += Insight(
                    InsightKind.STRENGTH, "${s.subject.name} 점수가 ${t.compact()}점 올랐어요",
                    "상승세입니다. 지금 방식을 유지하면서 다음 단원 예습으로 이어가 보세요.", s.subject.id,
                )
            }
        }

        // 3. 반 평균 대비
        scores.forEach { s ->
            val d = s.vsClass ?: return@forEach
            if (d <= -10) out += Insight(
                InsightKind.WEAKNESS, "${s.subject.name} 반 평균보다 ${(-d).compact()}점 낮아요",
                "기본 개념 확인이 우선입니다. 교과서 예제 위주로 복습해 보세요.", s.subject.id,
            )
        }

        // 4. 복습 밀림 / 예습 제안 (진도 기준)
        progress.forEach { p ->
            val reviewBehind = p.classCovered - p.reviewed
            if (reviewBehind >= 3) {
                val first = p.reviewQueue.firstOrNull()
                out += Insight(
                    InsightKind.ALERT, "${p.subject.name} 복습이 ${reviewBehind}개 단원 밀렸어요",
                    "수업은 진행됐지만 복습하지 않은 단원이 쌓였습니다. 오늘 '${first?.title ?: "첫 단원"}'부터 시작해 보세요.",
                    p.subject.id,
                    action = first?.let { InsightAction.CreateTask("${p.subject.name} ${it.title} 복습", p.subject.id, it.id, TaskType.REVIEW) },
                )
            }
            val next = p.previewQueue.firstOrNull()
            if (next != null && reviewBehind <= 1) {
                out += Insight(
                    InsightKind.SUGGESTION, "${p.subject.name} 다음 단원 예습 추천",
                    "곧 배울 '${next.title}'을(를) 미리 훑어보면 수업 이해도가 올라갑니다.",
                    p.subject.id,
                    action = InsightAction.CreateTask("${p.subject.name} ${next.title} 예습", p.subject.id, next.id, TaskType.PREVIEW),
                )
            }
        }

        // 5. 학습 시간 균형
        val weekTotal = weekly.sumOf { it.minutes }
        weekly.filter { it.subject != null && it.goalMinutes > 0 }.forEach { w ->
            val ratio = w.minutes.toFloat() / w.goalMinutes
            if (ratio < 0.4f) out += Insight(
                InsightKind.SUGGESTION, "${w.subject!!.name} 학습 시간이 부족해요",
                "이번 주 ${DateUtils.formatMinutes(w.minutes)} / 목표 ${DateUtils.formatMinutes(w.goalMinutes)}. 남은 요일에 나눠서 채워 보세요.",
                w.subject.id,
            )
        }
        if (weekTotal > 0) {
            val top = weekly.filter { it.subject != null }.maxByOrNull { it.minutes }
            if (top != null && top.minutes.toFloat() / weekTotal > 0.6f && subjects.size > 1) {
                out += Insight(
                    InsightKind.SUGGESTION, "${top.subject!!.name}에 시간이 몰려 있어요",
                    "이번 주 학습 시간의 ${(top.minutes * 100 / weekTotal)}%가 한 과목입니다. 다른 과목에도 시간을 배분해 보세요.",
                    top.subject.id,
                )
            }
        }

        // 6. 시간대 패턴
        val byHour = StudyStats.minutesByHour(sessions)
        val total = byHour.sum()
        if (total >= 180) {
            val bestHour = byHour.indices.maxByOrNull { byHour[it] }!!
            out += Insight(
                InsightKind.STRENGTH, "${bestHour}시~${bestHour + 1}시에 가장 집중해요",
                "이 시간대에 학습이 가장 많이 쌓였습니다. 어려운 과목을 이 시간에 배치해 보세요.",
            )
        }

        // 7. 다가오는 시험
        StudyStats.upcomingExams(events, tasks, withinDays = 14).forEach { exam ->
            val hasPrep = tasks.any { !it.done && it.type == TaskType.EXAM_PREP && (exam.subjectId == null || it.subjectId == exam.subjectId) }
            if (!hasPrep) {
                val subjectName = subjects.firstOrNull { it.id == exam.subjectId }?.name
                out += Insight(
                    InsightKind.ALERT, "${exam.title} ${DateUtils.dDay(exam.date)}",
                    "시험 준비 계획이 없습니다. 지금 준비 항목을 만들어 남은 기간에 나눠 보세요.",
                    exam.subjectId,
                    action = InsightAction.CreateTask("${subjectName?.let { "$it " } ?: ""}${exam.title} 준비", exam.subjectId, null, TaskType.EXAM_PREP),
                )
            }
        }

        // 8. 밀린 할 일
        val overdue = StudyStats.overdueTasks(tasks)
        if (overdue.isNotEmpty()) {
            out += Insight(
                InsightKind.ALERT, "기한이 지난 할 일이 ${overdue.size}개 있어요",
                "'${overdue.first().title}' 등이 미완료입니다. 오늘 처리하거나 기한을 옮겨 주세요.",
            )
        }

        // 9. 데이터가 없을 때 안내
        if (out.isEmpty()) {
            out += Insight(
                InsightKind.SUGGESTION, "데이터를 쌓아 보세요",
                "성적, 학습 시간, 단원 진도를 기록하면 강점·약점 분석과 맞춤 제안이 여기 표시됩니다.",
            )
        }

        val order = listOf(InsightKind.ALERT, InsightKind.WEAKNESS, InsightKind.SUGGESTION, InsightKind.STRENGTH)
        return out.sortedBy { order.indexOf(it.kind) }
    }

}
