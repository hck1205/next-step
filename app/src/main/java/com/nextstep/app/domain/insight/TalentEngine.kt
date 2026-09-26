package com.nextstep.app.domain.insight

import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.stats.SubjectScore
import com.nextstep.app.domain.text.compact
import com.nextstep.app.domain.time.DateUtils
import kotlin.math.sqrt

/**
 * 학부모용 재능 발견. 성적·학습 시간·진도 패턴에서 강점 신호를 찾습니다. 신호 하나 = 함수 하나.
 */
object TalentEngine {
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
        val scores = StudyStats.subjectScores(grades, subjects)
        val totalMinutes = sessions.sumOf { it.durationMinutes }
        val out = buildList {
            addAll(efficient(scores, subjects.size, sessions, totalMinutes))
            subjects.forEach { addAll(gradePattern(it, grades)) }
            addAll(listOfNotNull(consistency(sessions), focus(sessions, subjects), selfDirected(topics), timeOfDay(sessions, totalMinutes)))
            addAll(subjects.mapNotNull { confident(it, topics) })
        }
        return out.sortedByDescending { it.strength }
    }

    /** 효율: 학습 시간 비중은 낮은데 평균이 높은 과목 */
    private fun efficient(scores: List<SubjectScore>, subjectCount: Int, sessions: List<StudySessionEntity>, totalMinutes: Int): List<Talent> {
        if (totalMinutes < EFFICIENT_MIN_MINUTES || scores.size < 2) return emptyList()
        val avgShare = 1f / subjectCount
        return scores.mapNotNull { s ->
            val minutes = sessions.filter { it.subjectId == s.subject.id }.sumOf { it.durationMinutes }
            val share = minutes.toFloat() / totalMinutes
            if (s.average < HIGH_AVERAGE || share > avgShare * LOW_SHARE) return@mapNotNull null
            Talent("${s.subject.name}: 효율형 강점", "학습 시간 비중은 ${(share * PERCENT).toInt()}%인데 평균 ${s.average.compact()}점이에요. 적은 시간으로 성과를 내는 과목입니다. 심화 학습을 붙여 볼 만해요.", s.subject.id, 0.9f)
        }
    }

    /** 성장세(최근 3회 연속 오름)와 안정적인 실력(높은 평균·작은 편차) */
    private fun gradePattern(subject: SubjectEntity, grades: List<GradeEntity>): List<Talent> {
        val list = grades.filter { it.subjectId == subject.id }.sortedBy { it.date }.map { it.percent }
        if (list.size < RECENT_EXAMS) return emptyList()
        val (a, b, c) = list.takeLast(RECENT_EXAMS)
        val rising = if (a < b && b < c) {
            Talent("${subject.name}: 꾸준한 성장세", "최근 3번의 시험이 ${a.compact()} → ${b.compact()} → ${c.compact()}점으로 계속 올랐어요. 노력이 결과로 이어지는 과목입니다.", subject.id, 0.85f)
        } else null
        val avg = list.average()
        val sd = sqrt(list.map { (it - avg) * (it - avg) }.average())
        val stable = if (avg >= STABLE_AVERAGE && sd < STABLE_SD) {
            Talent("${subject.name}: 안정적인 실력", "평균 ${avg.compact()}점을 편차 ${sd.compact()}점으로 꾸준히 유지해요. 기복이 없다는 건 개념이 탄탄하다는 뜻이에요.", subject.id, 0.7f)
        } else null
        return listOfNotNull(rising, stable)
    }

    /** 꾸준함: 최근 14일 중 학습한 날 */
    private fun consistency(sessions: List<StudySessionEntity>): Talent? {
        val activeDays = StudyStats.dailyMinutes(sessions, CONSISTENCY_DAYS).count { it.minutes > 0 }
        if (activeDays < CONSISTENCY_ACTIVE) return null
        return Talent("꾸준함", "최근 14일 중 ${activeDays}일 공부했어요. 습관이 잡혀 있어요. 결과보다 이 꾸준함을 칭찬해 주세요.", null, 0.8f)
    }

    /** 몰입: 90분 이상 이어서 공부한 세션 */
    private fun focus(sessions: List<StudySessionEntity>, subjects: List<SubjectEntity>): Talent? {
        val s = sessions.maxByOrNull { it.durationMinutes }?.takeIf { it.durationMinutes >= FOCUS_MINUTES } ?: return null
        val name = subjects.firstOrNull { it.id == s.subjectId }?.name
        return Talent("몰입력", "한 번에 ${DateUtils.formatMinutes(s.durationMinutes)} 이어서 공부한 기록이 있어요${name?.let { " ($it)" } ?: ""}. 집중이 필요한 과목에 이 시간을 활용해 보세요.", s.subjectId, 0.6f)
    }

    /** 자기주도: 수업 전 예습 비율 */
    private fun selfDirected(topics: List<TopicEntity>): Talent? {
        val covered = topics.filter { it.classCovered }
        if (covered.size < SELF_MIN_COVERED) return null
        val ratio = covered.count { it.status == TopicStatus.PREVIEWED }.toFloat() / covered.size
        if (ratio < SELF_RATIO) return null
        return Talent("자기주도 학습", "배운 단원의 ${(ratio * PERCENT).toInt()}%를 미리 예습했어요. 스스로 앞서 나가는 성향이 있어요.", null, 0.75f)
    }

    /** 시간대 성향: 오전 또는 저녁에 몰린 공부 */
    private fun timeOfDay(sessions: List<StudySessionEntity>, totalMinutes: Int): Talent? {
        if (totalMinutes < TIME_OF_DAY_MIN_MINUTES) return null
        val byHour = StudyStats.minutesByHour(sessions)
        val morning = MORNING.sumOf { byHour[it] }
        val night = NIGHT.sumOf { byHour[it] }
        return when {
            morning.toFloat() / totalMinutes >= MORNING_SHARE -> Talent("아침형 학습자", "학습의 ${(morning * PERCENT / totalMinutes)}%가 오전에 이뤄져요. 아침 시간을 지켜 주면 성과가 좋아요.", null, 0.5f)
            night.toFloat() / totalMinutes >= NIGHT_SHARE -> Talent("저녁 집중형", "학습의 ${(night * PERCENT / totalMinutes)}%가 저녁 8시 이후예요. 이 시간대를 방해받지 않게 배려해 주세요.", null, 0.5f)
            else -> null
        }
    }

    /** 이해도 자기평가가 높은 과목 */
    private fun confident(subject: SubjectEntity, topics: List<TopicEntity>): Talent? {
        val ts = topics.filter { it.subjectId == subject.id && it.confidence > 0 }
        if (ts.size < CONFIDENT_MIN_TOPICS) return null
        val average = ts.map { it.confidence }.average()
        if (average < CONFIDENT_AVERAGE) return null
        return Talent("${subject.name}: 높은 이해 자신감", "단원 이해도를 평균 ${average.toInt()}%로 평가했어요. 자신감이 있는 과목이니 발표·경시 등 확장 활동을 권해 볼 수 있어요.", subject.id, 0.55f)
    }

    private const val PERCENT = 100
    private const val EFFICIENT_MIN_MINUTES = 120
    private const val HIGH_AVERAGE = 80
    private const val LOW_SHARE = 0.8f
    private const val RECENT_EXAMS = 3
    private const val STABLE_AVERAGE = 75
    private const val STABLE_SD = 5
    private const val CONSISTENCY_DAYS = 14
    private const val CONSISTENCY_ACTIVE = 9
    private const val FOCUS_MINUTES = 90
    private const val SELF_MIN_COVERED = 4
    private const val SELF_RATIO = 0.5f
    private const val TIME_OF_DAY_MIN_MINUTES = 180
    private const val MORNING_SHARE = 0.4f
    private const val NIGHT_SHARE = 0.5f
    private const val CONFIDENT_MIN_TOPICS = 3
    private const val CONFIDENT_AVERAGE = 80
    private val MORNING = 5..11
    private val NIGHT = 20..23
}
