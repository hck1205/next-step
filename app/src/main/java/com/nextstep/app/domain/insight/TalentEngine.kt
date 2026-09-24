package com.nextstep.app.domain.insight

import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.text.compact
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.time.DateUtils

/**
 * 학부모용 재능 발견. 성적·학습 시간·진도 패턴에서 강점 신호를 찾습니다.
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
                    out += Talent("${s.subject.name}: 효율형 강점", "학습 시간 비중은 ${(share * 100).toInt()}%인데 평균 ${s.average.compact()}점이에요. 적은 시간으로 성과를 내는 과목입니다. 심화 학습을 붙여 볼 만해요.", s.subject.id, 0.9f)
                }
            }
        }

        // 성장세: 최근 3회 시험이 계속 오름
        subjects.forEach { subject ->
            val list = grades.filter { it.subjectId == subject.id }.sortedBy { it.date }.map { it.percent }
            if (list.size >= 3) {
                val last3 = list.takeLast(3)
                if (last3[0] < last3[1] && last3[1] < last3[2]) {
                    out += Talent("${subject.name}: 꾸준한 성장세", "최근 3번의 시험이 ${last3[0].compact()} → ${last3[1].compact()} → ${last3[2].compact()}점으로 계속 올랐어요. 노력이 결과로 이어지는 과목입니다.", subject.id, 0.85f)
                }
                val avg = list.average()
                val sd = Math.sqrt(list.map { (it - avg) * (it - avg) }.average())
                if (avg >= 75 && sd < 5 && list.size >= 3) {
                    out += Talent("${subject.name}: 안정적인 실력", "평균 ${avg.compact()}점을 편차 ${sd.compact()}점으로 꾸준히 유지해요. 기복이 없다는 건 개념이 탄탄하다는 뜻이에요.", subject.id, 0.7f)
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
            val previewedBefore = covered.count { it.status == TopicStatus.PREVIEWED }
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

}
