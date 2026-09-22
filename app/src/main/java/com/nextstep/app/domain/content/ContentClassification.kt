package com.nextstep.app.domain.content

import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel

/** 자동 분류 결과. 사용자가 등록 화면에서 수정할 수 있습니다. */
data class ContentClassification(
    val subjectKey: String,
    val gradeLevel: GradeLevel,
    val contentType: ContentType,
    val keywords: List<String>,
    /** 분류 근거. UI 에서 "왜 이렇게 분류했는지" 보여주고 나중에 모델 학습 데이터로 씁니다. */
    val reasons: List<String>,
)
