package com.nextstep.app.domain.mentor

import com.nextstep.app.data.local.entity.SubjectEntity

/** 과목 하나의 과제 완료 현황. [subject] 가 null 이면 과목 없는 과제. */
data class AssignmentSubject(val subject: SubjectEntity?, val done: Int, val total: Int)
