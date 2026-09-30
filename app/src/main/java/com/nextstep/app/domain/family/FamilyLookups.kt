package com.nextstep.app.domain.family

import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.SubjectEntity

/** 가족의 학생(가족 하나 = 학생 한 명). 아직 없으면 null. */
fun List<MemberEntity>.student(): MemberEntity? = firstOrNull { it.isStudent && !it.deleted }

/** id 로 과목 찾기. id 가 없거나 맞는 과목이 없으면 null. */
fun List<SubjectEntity>.byId(id: String?): SubjectEntity? = id?.let { key -> firstOrNull { it.id == key } }
