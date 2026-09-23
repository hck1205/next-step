package com.nextstep.app.domain.curriculum

import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.local.entity.PeerTopicEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity

/** 한 구간의 커리큘럼을 가족 데이터와 대조한 결과. */
data class CurriculumPlan(
    val curriculum: TermCurriculum,
    val subjects: List<SubjectPlan>,
    /** 카탈로그에 없지만 다른 가족들이 이 시기에 등록한 단원(참고). */
    val peerExtras: List<PeerTopicEntity>,
) {
    val essentialTodo: List<UnitPlan> get() = subjects.flatMap { it.units }.filter { it.unit.essential && it.status == UnitStatus.NOT_REGISTERED }
    val registeredRatio: Float get() {
        val all = subjects.flatMap { it.units }
        if (all.isEmpty()) return 0f
        return all.count { it.status != UnitStatus.NOT_REGISTERED }.toFloat() / all.size
    }
}

data class SubjectPlan(
    val subject: String,
    /** 가족에 등록된 같은 이름의 과목. 없으면 "가져오기"로 만듭니다. */
    val familySubject: SubjectEntity?,
    val units: List<UnitPlan>,
) {
    val notRegistered: List<CurriculumUnit> get() = units.filter { it.status == UnitStatus.NOT_REGISTERED }.map { it.unit }
}

data class UnitPlan(
    val unit: CurriculumUnit,
    val status: UnitStatus,
    val matchedTopic: TopicEntity?,
    /** 같은 단원을 등록한 다른 가족 수. 모르면 null. */
    val peerFamilies: Int?,
    /** 저장소에서 찾은 영상(최대 2개). */
    val videos: List<ContentEntity>,
    /** 저장소에 없을 때 열 유튜브 검색 주소. */
    val searchUrl: String,
    /** 지금 할 일 제안 한 줄. 없으면 null. */
    val suggestion: String?,
)

enum class UnitStatus(val label: String) {
    NOT_REGISTERED("미등록"),
    REGISTERED("등록됨"),
    IN_CLASS("학교 진도 중"),
    DONE("끝냄"),
}
