package com.nextstep.app.domain.curriculum

import com.nextstep.app.data.local.entity.SubjectEntity

data class SubjectPlan(
    val subject: String,
    /** 가족에 등록된 같은 이름의 과목. 없으면 "가져오기"로 만듭니다. */
    val familySubject: SubjectEntity?,
    val units: List<UnitPlan>,
) {
    val notRegistered: List<CurriculumUnit> get() = units.filter { it.status == UnitStatus.NOT_REGISTERED }.map { it.unit }
}
