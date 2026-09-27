package com.nextstep.app.ui.parent.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.hub.ConcernSection
import com.nextstep.app.domain.today.ParentTodayCard
import com.nextstep.app.ui.common.UiDefaults
import com.nextstep.app.ui.components.card.EmptyCard
import com.nextstep.app.ui.components.card.GoalFocusCard
import com.nextstep.app.ui.components.card.JourneyNowCard
import com.nextstep.app.ui.components.card.MissionFocusCard
import com.nextstep.app.ui.components.card.RewardDueCard
import com.nextstep.app.ui.components.card.RoutineCard
import com.nextstep.app.ui.components.card.UpcomingExamCard
import com.nextstep.app.ui.components.card.WeekPlanCard
import com.nextstep.app.ui.components.row.EventRow
import com.nextstep.app.ui.parent.ParentDashboardActions
import com.nextstep.app.ui.parent.ParentDashboardEvent
import com.nextstep.app.ui.parent.ParentDashboardUiState

/** 학부모 오늘 화면의 카드 한 장의 내용. 슬라이드에서는 줄인 모양([compact]), 관심사 칩·자세히 시트에서는 전부. */
@Composable
internal fun ParentCardBody(card: ParentTodayCard, state: ParentDashboardUiState, actions: ParentDashboardActions, onEvent: (ParentDashboardEvent) -> Unit, compact: Boolean) {
    val rows = if (compact) UiDefaults.MAX_ROWS else FULL_ROWS
    when (card) {
        ParentTodayCard.JOURNEY -> JourneyNowCard(items = state.journeyNow, today = state.today, hasBirthDate = state.hasBirthDate, onOpen = actions.onOpenJourney)
        ParentTodayCard.REWARDS -> RewardDueCard(state.rewardsDue, onGive = { onEvent(ParentDashboardEvent.GiveReward(it)) }, onOpenGoal = actions.onOpenGoal)
        ParentTodayCard.GOALS -> Column {
            GoalFocusCard(state.goalFocus, onOpen = actions.onOpenGoal)
            if (!compact) TextButton(onClick = { actions.onOpenRecords(ConcernSection.GOAL_TREE) }) { Text("목표 전체") }
        }
        ParentTodayCard.TODAY -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (state.pendingTasks.isEmpty() && state.todayEvents.isEmpty()) EmptyCard("오늘은 잡힌 할 일과 일정이 없어요")
            state.pendingTasks.take(rows).forEach { t -> PendingTaskRow(t, state.subjects.firstOrNull { it.id == t.subjectId }) }
            state.todayEvents.take(rows).forEach { occ -> EventRow(occ, state.subjects) }
            if (!compact) TextButton(onClick = { actions.onOpenRecords(ConcernSection.CALENDAR) }) {
                Text(if (state.pendingTasks.size > rows) "할 일 ${state.pendingTasks.size - rows}개 더 · 일정 전체" else "일정 전체")
            }
        }
        ParentTodayCard.WEEK -> state.week?.let { week ->
            WeekPlanCard(
                week = week, access = state.weekAccess,
                onSavePlan = { goals, minutes -> onEvent(ParentDashboardEvent.SaveWeekPlan(goals, minutes)) },
                onToggle = { id, i -> onEvent(ParentDashboardEvent.ToggleWeekGoal(id, i)) },
                onApprove = { onEvent(ParentDashboardEvent.ApproveWeek(it)) },
                onReflect = { w, mood, good, hard, change -> onEvent(ParentDashboardEvent.ReflectWeek(w, mood, good, hard, change)) },
                onOpen = { actions.onOpenRecords(ConcernSection.SELF) },
            )
        }
        ParentTodayCard.ROUTINE -> RoutineCard(state.routines, onToggle = { p, item -> onEvent(ParentDashboardEvent.ToggleRoutine(p, item)) }, onOpen = actions.onOpenProject, compact = compact)
        ParentTodayCard.MISSIONS -> MissionFocusCard(state.missionFocus, onOpen = actions.onOpenGoals)
        ParentTodayCard.EXAM -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            state.upcomingExams.take(if (compact) 1 else rows).forEach { UpcomingExamCard(it) }
        }
    }
}

/** 카드 틀의 제목. 스스로 머리를 가진 카드(약속한 보상·이번 주 계획·다가오는 시험)는 빈 문자열. */
internal fun parentCardTitle(card: ParentTodayCard, state: ParentDashboardUiState): String = when (card) {
    ParentTodayCard.TODAY -> "오늘의 ${state.studentName.ifBlank { "아이" }}"
    ParentTodayCard.REWARDS, ParentTodayCard.WEEK, ParentTodayCard.EXAM -> ""
    ParentTodayCard.JOURNEY, ParentTodayCard.GOALS, ParentTodayCard.ROUTINE, ParentTodayCard.MISSIONS -> card.title
}

private const val FULL_ROWS = 10
