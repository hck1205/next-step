package com.nextstep.app.domain.period

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.local.entity.CheerEntity
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity

/** 기간 리포트·성장 앨범이 읽는 기록 한 벌(지운 것은 계산할 때 뺍니다). */
data class PeriodRecords(
    val sessions: List<StudySessionEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val grades: List<GradeEntity> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val activities: List<ActivityEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val cheers: List<CheerEntity> = emptyList(),
)
