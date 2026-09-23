package com.nextstep.app.domain.curriculum

import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.local.entity.PeerTopicEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.content.YouTubeLinks

/**
 * 학기 카탈로그를 가족의 과목·단원·진도, 또래 통계, 콘텐츠 저장소와 대조합니다. 순수 함수입니다.
 * 판단은 세 가지뿐입니다: 이 단원이 등록돼 있는가(이름 토큰 일치), 등록됐다면 어디까지 갔는가, 볼 만한 영상이 있는가.
 */
object CurriculumRecommender {
    private const val MAX_VIDEOS = 2
    private const val MAX_PEER_EXTRAS = 3

    fun plan(
        curriculum: TermCurriculum,
        subjects: List<SubjectEntity>,
        topics: List<TopicEntity>,
        peerTopics: List<PeerTopicEntity>,
        contents: List<ContentEntity>,
        gradeLevel: GradeLevel,
        gradeLabel: String,
    ): CurriculumPlan {
        val peersHere = peerTopics.filter { !it.deleted && it.periodKey == curriculum.periodKey }
        val subjectPlans = curriculum.subjects.map { name ->
            val family = subjects.firstOrNull { !it.deleted && sameSubject(it.name, name) }
            val familyTopics = family?.let { f -> topics.filter { !it.deleted && it.subjectId == f.id } }.orEmpty()
            val units = curriculum.unitsOf(name).map { unit ->
                val topic = familyTopics.firstOrNull { matches(unit, it.title) }
                val status = statusOf(topic)
                UnitPlan(
                    unit = unit,
                    status = status,
                    matchedTopic = topic,
                    peerFamilies = peersHere.filter { sameSubject(it.subject, name) && matches(unit, it.title) }.maxOfOrNull { it.families },
                    videos = videosFor(unit, contents, gradeLevel),
                    searchUrl = YouTubeLinks.searchUrl("$gradeLabel $name ${unit.title} 개념"),
                    suggestion = suggestion(unit, status, topic),
                )
            }
            SubjectPlan(name, family, units)
        }
        val catalogUnits = curriculum.units
        val extras = peersHere.filter { p -> catalogUnits.none { u -> sameSubject(p.subject, u.subject) && matches(u, p.title) } }
            .sortedByDescending { it.families }.take(MAX_PEER_EXTRAS)
        return CurriculumPlan(curriculum, subjectPlans, extras)
    }

    /** 과목명 비교: "수학" 과 "중1 수학", "통합과학1" 과 "과학" 처럼 한쪽이 다른 쪽을 품으면 같은 과목. */
    fun sameSubject(a: String, b: String): Boolean {
        val x = a.trim(); val y = b.trim()
        return x.isNotEmpty() && y.isNotEmpty() && (x.contains(y) || y.contains(x))
    }

    /** 단원 제목·키워드 토큰 하나라도 등록 단원 제목에 들어 있으면 같은 단원으로 봅니다. */
    fun matches(unit: CurriculumUnit, title: String): Boolean {
        val hay = title.lowercase()
        return unit.matchTokens.any { hay.contains(it.lowercase()) }
    }

    fun statusOf(topic: TopicEntity?): UnitStatus = when {
        topic == null -> UnitStatus.NOT_REGISTERED
        topic.status == TopicStatus.REVIEWED || topic.status == TopicStatus.MASTERED -> UnitStatus.DONE
        topic.classCovered || topic.status == TopicStatus.IN_CLASS -> UnitStatus.IN_CLASS
        else -> UnitStatus.REGISTERED
    }

    private fun videosFor(unit: CurriculumUnit, contents: List<ContentEntity>, gradeLevel: GradeLevel): List<ContentEntity> =
        contents.filter { c -> !c.deleted && (c.gradeLevel == GradeLevel.ALL || c.gradeLevel == gradeLevel) && matches(unit, c.title + " " + c.keywords) }
            .sortedWith(compareByDescending<ContentEntity> { if (it.ratingCount == 0) 0.0 else it.ratingSum.toDouble() / it.ratingCount }.thenByDescending { it.ratingCount })
            .take(MAX_VIDEOS)

    private fun suggestion(unit: CurriculumUnit, status: UnitStatus, topic: TopicEntity?): String? = when {
        status == UnitStatus.NOT_REGISTERED && unit.essential -> "뼈대 단원이에요. 내 과목에 넣고 개념 영상 하나부터"
        status == UnitStatus.IN_CLASS && topic?.status == TopicStatus.NOT_STARTED -> "학교에서 나간 단원이에요. 이번 주 복습으로"
        status == UnitStatus.REGISTERED && unit.essential -> "곧 학교에서 다룰 뼈대 단원. 예습 영상 한 편이면 충분해요"
        else -> null
    }
}
