package com.nextstep.app.domain.curriculum

import com.nextstep.app.data.local.entity.PeerTopicEntity
import com.nextstep.app.domain.text.ratioOf

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
        return ratioOf(all.count { it.status != UnitStatus.NOT_REGISTERED }, all.size)
    }
}
