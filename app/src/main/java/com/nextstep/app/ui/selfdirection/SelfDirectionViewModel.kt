package com.nextstep.app.ui.selfdirection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.MemberRepository
import com.nextstep.app.data.repository.WeekPlanRepository
import com.nextstep.app.domain.family.StudentContext
import com.nextstep.app.domain.selfdirection.SelfDirection
import com.nextstep.app.domain.selfdirection.SelfDirectionStage
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 기록 › 공부 › 스스로. 자기주도 단계·흔적·제안을 계산하고, 주간 계획·돌아보기·단계 바꾸기를 저장합니다. */
class SelfDirectionViewModel(
    streams: FamilyDataStreams,
    private val weekPlans: WeekPlanRepository,
    private val members: MemberRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {

    val state: StateFlow<SelfDirectionUiState> = combine(streams.profile, streams.members, streams.weekPlans, streams.sessions, streams.tasks) { profile, all, plans, sessions, tasks ->
        val day = today()
        val student = StudentContext.of(all, day).student
        val report = SelfDirection.report(student, plans, sessions, tasks, day)
        val thisWeek = DateUtils.weekStart(day).toEpochDay()
        SelfDirectionUiState(
            loaded = true, studentName = profile.studentName, studentId = student?.id, report = report,
            week = SelfDirection.week(report.stage, plans, sessions, day),
            history = plans.filter { !it.deleted && it.weekStart < thisWeek && (it.hasPlan || it.isReflected) }.sortedByDescending { it.weekStart },
        )
    }.asUiState(viewModelScope, SelfDirectionUiState())

    fun onEvent(event: SelfDirectionEvent) {
        when (event) {
            is SelfDirectionEvent.SavePlan -> viewModelScope.launch { weekPlans.savePlan(DateUtils.weekStart(today()), event.goals, event.minutes) }
            is SelfDirectionEvent.ToggleGoal -> viewModelScope.launch { weekPlans.toggleGoal(event.planId, event.index) }
            is SelfDirectionEvent.Approve -> viewModelScope.launch { weekPlans.approve(event.planId) }
            is SelfDirectionEvent.Reflect -> viewModelScope.launch { weekPlans.reflect(event.week, event.mood, event.good, event.hard, event.change) }
            is SelfDirectionEvent.SetStage -> setStage(event.stage)
        }
    }

    /** 기본 단계와 같은 단계를 고르면 "자동"으로 되돌립니다. */
    private fun setStage(stage: SelfDirectionStage?) = viewModelScope.launch {
        val s = state.value
        val id = s.studentId ?: return@launch
        members.setSelfDirection(id, stage?.takeIf { it != s.report?.defaultStage })
    }
}
