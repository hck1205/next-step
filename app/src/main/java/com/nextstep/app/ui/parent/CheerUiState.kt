package com.nextstep.app.ui.parent

import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.stats.DayMinutes

/** 학부모 격려 화면. 오늘 자녀가 한 일을 근거로 구체적인 칭찬을 돕습니다. */
data class CheerUiState(
    val studentName: String = "",
    val myName: String = "",
    val streak: Int = 0,
    val todayMinutes: Int = 0,
    val todayDoneTasks: Int = 0,
    val todayTopicsReviewed: Int = 0,
    val weekMinutes: Int = 0,
    val daily: List<DayMinutes> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    /** 데이터에 맞춰 만든 칭찬 문구 제안. */
    val cheerSuggestions: List<String> = emptyList(),
    /** 단계별 칭찬 방향. 단계가 없으면 null. */
    val praiseStyle: String? = null,
)
