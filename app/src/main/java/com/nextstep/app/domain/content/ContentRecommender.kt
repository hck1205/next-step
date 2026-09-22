package com.nextstep.app.domain.content

import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.domain.stats.SubjectScore
import com.nextstep.app.domain.stats.UpcomingExam
import com.nextstep.app.domain.stats.SubjectProgress
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
        val weakSubjects = scores.filter { it.average < 70 }.map { it.subject.name }.toSet()
        val examSubjects = upcomingExams.filter { it.date.toEpochDay() - DateUtils.today().toEpochDay() <= 14 }
            .mapNotNull { e -> subjects.firstOrNull { it.id == e.subjectId }?.name }.toSet()
        val reviewTopics: List<Pair<String, TopicEntity>> = progress.flatMap { p -> p.reviewQueue.map { p.subject.name to it } }
        val previewTopics: List<Pair<String, TopicEntity>> = progress.flatMap { p -> p.previewQueue.take(1).map { p.subject.name to it } }

        return contents.filter { !it.deleted && !it.watched }.mapNotNull { c ->
            var score = 0f
            var reason = ""
            val subjectMatch = c.subjectKey.isNotEmpty() && subjects.any { it.name == c.subjectKey }
            if (subjectMatch) score += 1f

            fun topicHit(list: List<Pair<String, TopicEntity>>): TopicEntity? = list.firstOrNull { (subj, t) ->
                (c.subjectKey.isEmpty() || c.subjectKey == subj) && matches(c, t.title)
            }?.second
            topicHit(reviewTopics)?.let { score += 4f; reason = "복습할 '${it.title}' 단원과 맞아요" }
            if (reason.isEmpty()) topicHit(previewTopics)?.let { score += 3f; reason = "예습할 '${it.title}' 단원과 맞아요" }
            if (c.subjectKey in examSubjects && c.contentType == ContentType.EXAM_PREP) { score += 3f; if (reason.isEmpty()) reason = "${c.subjectKey} 시험이 가까워요" }
            if (c.subjectKey in weakSubjects && c.contentType == ContentType.CONCEPT) { score += 2f; if (reason.isEmpty()) reason = "${c.subjectKey} 개념을 다시 잡기 좋아요" }
            if (gradeLevel != GradeLevel.ALL && c.gradeLevel == gradeLevel) score += 0.5f
            if (gradeLevel != GradeLevel.ALL && c.gradeLevel != GradeLevel.ALL && c.gradeLevel != gradeLevel) score -= 1.5f
            score += c.averageRating * 0.3f
            if (c.contentType == ContentType.STUDY_METHOD || c.contentType == ContentType.MOTIVATION) score += 0.3f
            if (reason.isEmpty()) reason = when {
                subjectMatch -> "${c.subjectKey} 과목 추천"
                c.contentType == ContentType.STUDY_METHOD -> "공부법 추천"
                c.contentType == ContentType.MOTIVATION -> "힘이 되는 영상"
                else -> "저장소 추천"
            }
            if (score <= 0f) null else ContentRecommendation(c, score, reason)
        }.sortedByDescending { it.score }.take(limit)
    }

    /** 단원 제목의 토큰이 콘텐츠 제목이나 키워드에 포함되는지. */
    fun matches(content: ContentEntity, topicTitle: String): Boolean {
        val tokens = topicTitle.split(Regex("[\\s.\\-:()\\[\\]]+")).map { it.trim() }.filter { it.length >= 2 && !it.all { c -> c.isDigit() } }
        if (tokens.isEmpty()) return false
        val hay = (content.title + " " + content.keywords).lowercase()
        return tokens.any { hay.contains(it.lowercase()) }
    }
}
