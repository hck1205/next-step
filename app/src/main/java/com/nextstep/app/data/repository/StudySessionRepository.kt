package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.prefs.RunningTimer
import kotlinx.coroutines.flow.Flow

/** 학습 기록과 진행 중 타이머. */
interface StudySessionRepository {
    val sessions: Flow<List<StudySessionEntity>>
    val runningTimer: Flow<RunningTimer?>
    suspend fun save(session: StudySessionEntity)
    suspend fun delete(id: String)
    suspend fun startTimer(subjectId: String?)
    /** 타이머를 멈추고 1분 이상이면 기록으로 저장합니다. 저장된 기록 또는 null. */
    suspend fun stopTimer(note: String = ""): StudySessionEntity?
    suspend fun cancelTimer()
}
