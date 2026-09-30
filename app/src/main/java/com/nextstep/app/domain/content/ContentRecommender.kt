package com.nextstep.app.domain.content

import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.domain.family.byId
import com.nextstep.app.domain.stats.SubjectProgress
import com.nextstep.app.domain.stats.SubjectScore
import com.nextstep.app.domain.stats.UpcomingExam
import com.nextstep.app.domain.time.DateUtils

/**
 * 콘텐츠 저장소에서 "지금 이 학생에게" 맞는 것을 고릅니다.
 * 근거: 복습/예습 대기 단원과 키워드 일치, 약한 과목의 개념 강의, 시험 임박 시 시험 대비, 평점.
 */
object ContentRecommender {

    fun recommend(
        contents: List<ContentEntity>,
        subjects: List<SubjectEntity>,
        progress: List<SubjectProgress>,
        scores: List<SubjectScore>,
        upcomingExams: List<UpcomingExam>,
        gradeLevel: GradeLevel = GradeLevel.ALL,
        limit: Int = 5,
    ): List<ContentRecommendation> {
        if (contents.isEmpty()) return emptyList()
        val signals = Signals(
            subjectNames = subjects.map { it.name }.toSet(),
            weakSubjects = scores.filter { it.average < WEAK_AVERAGE }.map { it.subject.name }.toSet(),
            examSubjects = upcomingExams.filter { it.date.toEpochDay() - DateUtils.today().toEpochDay() <= EXAM_WITHIN_DAYS }
                .mapNotNull { e -> subjects.byId(e.subjectId)?.name }.toSet(),
            reviewTopics = progress.flatMap { p -> p.reviewQueue.map { p.subject.name to it } },
            previewTopics = progress.flatMap { p -> p.previewQueue.take(1).map { p.subject.name to it } },
            gradeLevel = gradeLevel,
        )
        return contents.filter { !it.deleted && !it.watched }.mapNotNull { rank(it, signals) }.sortedByDescending { it.score }.take(limit)
    }

    /** 이 학생의 지금 신호. 콘텐츠마다 다시 계산하지 않도록 한 번 모읍니다. */
    private data class Signals(
        val subjectNames: Set<String>,
        val weakSubjects: Set<String>,
        val examSubjects: Set<String>,
        val reviewTopics: List<Pair<String, TopicEntity>>,
        val previewTopics: List<Pair<String, TopicEntity>>,
        val gradeLevel: GradeLevel,
    )

    /** 점수와 첫 번째로 맞은 근거. 점수가 0 이하면 추천하지 않습니다. */
    private fun rank(c: ContentEntity, s: Signals): ContentRecommendation? {
        var score = 0f
        var reason = ""
        val subjectMatch = c.subjectKey.isNotEmpty() && c.subjectKey in s.subjectNames
        if (subjectMatch) score += 1f
        fun topicHit(list: List<Pair<String, TopicEntity>>): TopicEntity? = list.firstOrNull { (subj, t) ->
            (c.subjectKey.isEmpty() || c.subjectKey == subj) && matches(c, t.title)
        }?.second
        topicHit(s.reviewTopics)?.let { score += 4f; reason = "복습할 '${it.title}' 단원과 맞아요" }
        if (reason.isEmpty()) topicHit(s.previewTopics)?.let { score += 3f; reason = "예습할 '${it.title}' 단원과 맞아요" }
        if (c.subjectKey in s.examSubjects && c.contentType == ContentType.EXAM_PREP) { score += 3f; if (reason.isEmpty()) reason = "${c.subjectKey} 시험이 가까워요" }
        if (c.subjectKey in s.weakSubjects && c.contentType == ContentType.CONCEPT) { score += 2f; if (reason.isEmpty()) reason = "${c.subjectKey} 개념을 다시 잡기 좋아요" }
        if (s.gradeLevel != GradeLevel.ALL && c.gradeLevel == s.gradeLevel) score += 0.5f
        if (s.gradeLevel != GradeLevel.ALL && c.gradeLevel != GradeLevel.ALL && c.gradeLevel != s.gradeLevel) score -= 1.5f
        score += c.averageRating * RATING_WEIGHT
        if (c.contentType == ContentType.STUDY_METHOD || c.contentType == ContentType.MOTIVATION) score += 0.3f
        if (reason.isEmpty()) reason = when {
            subjectMatch -> "${c.subjectKey} 과목 추천"
            c.contentType == ContentType.STUDY_METHOD -> "공부법 추천"
            c.contentType == ContentType.MOTIVATION -> "힘이 되는 영상"
            else -> "저장소 추천"
        }
        return if (score <= 0f) null else ContentRecommendation(c, score, reason)
    }

    /** 단원 제목의 토큰이 콘텐츠 제목이나 키워드에 포함되는지. */
    fun matches(content: ContentEntity, topicTitle: String): Boolean {
        val tokens = topicTitle.split(Regex("[\\s.\\-:()\\[\\]]+")).map { it.trim() }.filter { it.length >= 2 && !it.all { c -> c.isDigit() } }
        if (tokens.isEmpty()) return false
        val hay = (content.title + " " + content.keywords).lowercase()
        return tokens.any { hay.contains(it.lowercase()) }
    }

    private const val WEAK_AVERAGE = 70
    private const val EXAM_WITHIN_DAYS = 14
    private const val RATING_WEIGHT = 0.3f
}
