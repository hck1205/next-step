package com.nextstep.app.domain

import com.nextstep.app.data.local.EventEntity
import com.nextstep.app.data.local.GradeEntity
import com.nextstep.app.data.local.StudySessionEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.local.TaskEntity
import com.nextstep.app.data.local.TopicEntity
import com.nextstep.app.data.model.TaskType

enum class InsightKind(val label: String) {
    STRENGTH("강점"),
    WEAKNESS("보완 필요"),
    SUGGESTION("제안"),
    ALERT("주의"),
}

data class Insight(
    val kind: InsightKind,
    val title: String,
    val body: String,
    val subjectId: String? = null,
    /** 실행 제안이 있으면 버튼으로 노출. */
    val action: InsightAction? = null,
)

/** 인사이트에서 바로 만들 수 있는 실행 항목. */
sealed class InsightAction {
    data class CreateTask(val title: String, val subjectId: String?, val topicId: String?, val type: TaskType) : InsightAction()
}

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
                    "평균 ${best.average.fmt()}점으로 가장 높습니다. 이 과목의 학습 방식을 다른 과목에도 적용해 보세요.",
                    best.subject.id,
                )
            }
        }
        scores.filter { it.average < 70 }.sortedBy { it.average }.take(2).forEach { weak ->
            val queue = progress.firstOrNull { it.subject.id == weak.subject.id }?.reviewQueue?.firstOrNull()
            out += Insight(
                InsightKind.WEAKNESS, "${weak.subject.name} 보완이 필요해요",
                "평균 ${weak.average.fmt()}점입니다. 최근 배운 단원부터 복습하고 이번 주 학습 시간을 늘려 보세요.",
                weak.subject.id,
                action = InsightAction.CreateTask("${weak.subject.name} ${queue?.title ?: "핵심 단원"} 복습", weak.subject.id, queue?.id, TaskType.REVIEW),
            )
        }

        // 2. 추세
        scores.forEach { s ->
            val t = s.trend ?: return@forEach
            when {
                t <= -10 -> out += Insight(
                    InsightKind.ALERT, "${s.subject.name} 점수가 ${(-t).fmt()}점 떨어졌어요",
                    "직전 시험 대비 하락했습니다. 틀린 문제 유형을 정리하고 해당 단원을 다시 복습하세요.", s.subject.id,
                )
                t >= 10 -> out += Insight(
                    InsightKind.STRENGTH, "${s.subject.name} 점수가 ${t.fmt()}점 올랐어요",
                    "상승세입니다. 지금 방식을 유지하면서 다음 단원 예습으로 이어가 보세요.", s.subject.id,
                )
            }
        }

        // 3. 반 평균 대비
        scores.forEach { s ->
            val d = s.vsClass ?: return@forEach
            if (d <= -10) out += Insight(
                InsightKind.WEAKNESS, "${s.subject.name} 반 평균보다 ${(-d).fmt()}점 낮아요",
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

    // ------------------------------------------------------------------ 재능 발견 (학부모용)

    /**
     * 성적·학습 시간·진도 패턴에서 강점 신호를 찾습니다. 점수가 높은 과목뿐 아니라
     * 효율, 꾸준함, 성장세, 몰입, 자기주도성 같은 태도 재능도 함께 봅니다.
     */
    fun talents(
        subjects: List<SubjectEntity>,
        topics: List<TopicEntity>,
        grades: List<GradeEntity>,
        sessions: List<StudySessionEntity>,
    ): List<Talent> {
        val out = mutableListOf<Talent>()
        val scores = StudyStats.subjectScores(grades, subjects)
        val totalMinutes = sessions.sumOf { it.durationMinutes }

        // 효율: 학습 시간 비중은 낮은데 평균이 높은 과목
        if (totalMinutes >= 120 && scores.size >= 2) {
            val avgShare = 1f / subjects.size
            scores.forEach { s ->
                val minutes = sessions.filter { it.subjectId == s.subject.id }.sumOf { it.durationMinutes }
                val share = minutes.toFloat() / totalMinutes
                if (s.average >= 80 && share <= avgShare * 0.8f) {
                    out += Talent("${s.subject.name}: 효율형 강점", "학습 시간 비중은 ${(share * 100).toInt()}%인데 평균 ${s.average.fmt()}점이에요. 적은 시간으로 성과를 내는 과목입니다. 심화 학습을 붙여 볼 만해요.", s.subject.id, 0.9f)
                }
            }
        }

        // 성장세: 최근 3회 시험이 계속 오름
        subjects.forEach { subject ->
            val list = grades.filter { it.subjectId == subject.id }.sortedBy { it.date }.map { it.percent }
            if (list.size >= 3) {
                val last3 = list.takeLast(3)
                if (last3[0] < last3[1] && last3[1] < last3[2]) {
                    out += Talent("${subject.name}: 꾸준한 성장세", "최근 3번의 시험이 ${last3[0].fmt()} → ${last3[1].fmt()} → ${last3[2].fmt()}점으로 계속 올랐어요. 노력이 결과로 이어지는 과목입니다.", subject.id, 0.85f)
                }
                val avg = list.average()
                val sd = Math.sqrt(list.map { (it - avg) * (it - avg) }.average())
                if (avg >= 75 && sd < 5 && list.size >= 3) {
                    out += Talent("${subject.name}: 안정적인 실력", "평균 ${avg.fmt()}점을 편차 ${sd.fmt()}점으로 꾸준히 유지해요. 기복이 없다는 건 개념이 탄탄하다는 뜻이에요.", subject.id, 0.7f)
                }
            }
        }

        // 꾸준함: 최근 14일 중 학습한 날
        val daily = StudyStats.dailyMinutes(sessions, 14)
        val activeDays = daily.count { it.minutes > 0 }
        if (activeDays >= 9) out += Talent("꾸준함", "최근 14일 중 ${activeDays}일 공부했어요. 습관이 잡혀 있어요. 결과보다 이 꾸준함을 칭찬해 주세요.", null, 0.8f)

        // 몰입: 90분 이상 이어서 공부한 세션
        val longest = sessions.maxOfOrNull { it.durationMinutes } ?: 0
        if (longest >= 90) {
            val s = sessions.first { it.durationMinutes == longest }
            val name = subjects.firstOrNull { it.id == s.subjectId }?.name
            out += Talent("몰입력", "한 번에 ${DateUtils.formatMinutes(longest)} 이어서 공부한 기록이 있어요${name?.let { " ($it)" } ?: ""}. 집중이 필요한 과목에 이 시간을 활용해 보세요.", s.subjectId, 0.6f)
        }

        // 자기주도: 수업 전 예습 비율
        val covered = topics.filter { it.classCovered }
        if (covered.size >= 4) {
            val previewedBefore = covered.count { it.status.order >= com.nextstep.app.data.model.TopicStatus.PREVIEWED.order }
            val ratio = previewedBefore.toFloat() / covered.size
            if (ratio >= 0.5f) out += Talent("자기주도 학습", "배운 단원의 ${(ratio * 100).toInt()}%를 미리 예습했어요. 스스로 앞서 나가는 성향이 있어요.", null, 0.75f)
        }

        // 시간대 성향
        if (totalMinutes >= 180) {
            val byHour = StudyStats.minutesByHour(sessions)
            val morning = (5..11).sumOf { byHour[it] }
            val night = (20..23).sumOf { byHour[it] }
            when {
                morning.toFloat() / totalMinutes >= 0.4f -> out += Talent("아침형 학습자", "학습의 ${(morning * 100 / totalMinutes)}%가 오전에 이뤄져요. 아침 시간을 지켜 주면 성과가 좋아요.", null, 0.5f)
                night.toFloat() / totalMinutes >= 0.5f -> out += Talent("저녁 집중형", "학습의 ${(night * 100 / totalMinutes)}%가 저녁 8시 이후예요. 이 시간대를 방해받지 않게 배려해 주세요.", null, 0.5f)
            }
        }

        // 이해도 자기평가가 높은 과목
        subjects.forEach { subject ->
            val ts = topics.filter { it.subjectId == subject.id && it.confidence > 0 }
            if (ts.size >= 3 && ts.map { it.confidence }.average() >= 80) {
                out += Talent("${subject.name}: 높은 이해 자신감", "단원 이해도를 평균 ${ts.map { it.confidence }.average().toInt()}%로 평가했어요. 자신감이 있는 과목이니 발표·경시 등 확장 활동을 권해 볼 수 있어요.", subject.id, 0.55f)
            }
        }

        return out.sortedByDescending { it.strength }
    }

    private fun Double.fmt(): String = if (this == Math.floor(this)) toInt().toString() else String.format(java.util.Locale.ROOT, "%.1f", this)
}

/** 학부모 화면의 "재능 발견" 카드. strength 는 0~1 로 신호의 확신도. */
data class Talent(val title: String, val body: String, val subjectId: String?, val strength: Float)
