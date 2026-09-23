package com.nextstep.app.data.prefs

import androidx.datastore.core.DataStore

/** 진행 중인 학습 타이머. 앱이 종료돼도 복원되도록 DataStore 에 저장합니다. */
data class RunningTimer(val subjectId: String?, val startedAt: Long)
