package com.nextstep.app.ui.common

import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.feedback.FeedbackEngine
import com.nextstep.app.domain.feedback.Finding
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** 이번 주 피드백의 사실 한 벌(FeedbackEngine). 학생·학부모·멘토 오늘 화면이 같은 사실을 각자의 말(FeedbackVoice)로 보여 줍니다. */
fun FamilyDataStreams.weekFindings(today: () -> LocalDate): Flow<List<Finding>> =
    combine(subjects, topics, grades, sessions, tasks) { subjects, topics, grades, sessions, tasks ->
        FeedbackEngine.findings(subjects, topics, grades, sessions, tasks, today())
    }
