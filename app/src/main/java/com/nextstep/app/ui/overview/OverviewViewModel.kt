package com.nextstep.app.ui.overview

import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.data.local.entity.ObservationEntity
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.family.StudentContext
import com.nextstep.app.domain.health.GrowthStats
import com.nextstep.app.domain.hub.ConcernDigests
import com.nextstep.app.domain.insight.AptitudeEngine
import com.nextstep.app.domain.mission.MissionPlanner
import com.nextstep.app.domain.stats.BalanceStats
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/**
 * 기록 › 한눈에. 균형 판단(학습·자기주도·경험·연속)과, 관심사마다 요약 한 장을 만듭니다.
 * 자세한 내용은 각 관심사의 섹션 화면이 자기 ViewModel 로 그립니다.
 */
class OverviewViewModel(
    streams: FamilyDataStreams,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {

    private val base = combine(streams.profile, streams.members, streams.sessions, streams.tasks, streams.activities) { profile, members, sessions, tasks, activities ->
        val day = today()
        val ctx = StudentContext.of(members, day)
        Base(
            OverviewUiState(
                studentName = profile.studentName,
                stage = ctx.stage,
                currentPeriodLabel = ctx.currentPeriod?.label,
                yearLabel = ctx.year?.label,
                today = day,
                loaded = true,
            ),
            ctx,
            sessions,
            tasks,
            activities,
        )
    }

    private val progress = combine(streams.topics, streams.subjects) { topics, subjects -> StudyStats.subjectProgress(topics, subjects) }

    private val exams = combine(streams.goals, streams.goalSteps, streams.grades) { goals, steps, grades -> Exams(goals, steps, grades) }

    private val growth = combine(streams.growthRecords, streams.observations) { records, observations -> Growth(records, observations) }

    val state: StateFlow<OverviewUiState> = combine(base, progress, exams, growth, streams.events) { b, progress, e, g, events ->
        val s = b.state.copy(balance = BalanceStats.report(b.ctx.stage, b.sessions, b.tasks, b.activities, b.ctx.currentPeriod, b.state.today, events, b.ctx.year))
        s.copy(
            digests = listOf(
                ConcernDigests.study(s.balance?.weekMinutes ?: 0, progress),
                ConcernDigests.exams(MissionPlanner.focus(e.goals, e.steps, s.today), e.grades),
                ConcernDigests.growth(GrowthStats.summarize(g.records.filter { !it.deleted }, s.today)),
                ConcernDigests.discover(s.balance?.experiencesThisPeriod ?: 0, AptitudeEngine.signals(b.activities, g.observations, s.today)),
            ),
        )
    }.asUiState(viewModelScope, OverviewUiState())

    private data class Base(
        val state: OverviewUiState,
        val ctx: StudentContext,
        val sessions: List<StudySessionEntity>,
        val tasks: List<TaskEntity>,
        val activities: List<ActivityEntity>,
    )

    private data class Exams(val goals: List<GoalEntity>, val steps: List<GoalStepEntity>, val grades: List<GradeEntity>)

    private data class Growth(val records: List<GrowthRecordEntity>, val observations: List<ObservationEntity>)
}
