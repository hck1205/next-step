package com.nextstep.app.ui.yearplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.JourneyRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.growth.StudentScreen
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.domain.year.YearArea
import com.nextstep.app.domain.year.YearPlans
import com.nextstep.app.domain.year.YearTask
import com.nextstep.app.domain.year.YearTerm
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * "올해" 탭: 올해(만 나이·학년) 할 일을 분류별 탭으로 나누고, 완료 표시는 여정 저장소에 "year:" 키로 남깁니다(동기화됨).
 */
class YearPlanViewModel(
    streams: FamilyDataStreams,
    private val journey: JourneyRepository,
    private val tasks: TaskRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {

    val state: StateFlow<YearPlanUiState> = combine(streams.members, streams.journeyItems) { members, stored ->
        val day = today()
        val screen = StudentScreen.of(members.firstOrNull { it.isStudent }, day)
        val year = screen.year
        val done = stored.filter { !it.deleted && it.status == MilestoneStatus.DONE && it.templateId?.startsWith(YearTask.PREFIX) == true }.mapNotNull { it.templateId }.toSet()
        val views = year?.let { y -> YearPlans.forYear(y.key).map { YearTaskView(it, it.storageId(y.key) in done) } }.orEmpty()
        val current = YearTerm.current(day)
        YearPlanUiState(
            year = year,
            level = screen.level,
            tabs = if (views.isEmpty()) emptyList() else listOf(tab(null, ALL_LABEL, views, current)) +
                views.map { it.task.area }.distinct().sortedBy { it.ordinal }.map { area -> tab(area, area.label, views.filter { it.task.area == area }, current) },
            currentTerm = current,
            done = views.count { it.done },
            total = views.size,
            today = day,
            loaded = true,
        )
    }.asUiState(viewModelScope, YearPlanUiState())

    fun toggle(view: YearTaskView) = viewModelScope.launch {
        val year = state.value.year ?: return@launch
        journey.setTemplateStatus(view.task.storageId(year.key), if (view.done) MilestoneStatus.UPCOMING else MilestoneStatus.DONE, view.task.term.endDate(today()))
    }

    fun addToToday(task: YearTask) = viewModelScope.launch {
        tasks.save(TaskEntity(familyId = "", title = task.title, type = if (task.area == YearArea.EXAM) TaskType.EXAM_PREP else TaskType.HOMEWORK, dueDate = today().toEpochDay(), createdByRole = Role.STUDENT.name, note = task.how))
    }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: YearPlanEvent) {
        when (event) {
            is YearPlanEvent.Toggle -> toggle(event.view)
            is YearPlanEvent.AddToToday -> addToToday(event.task)
        }
    }

    private fun tab(area: YearArea?, label: String, views: List<YearTaskView>, current: YearTerm): YearTab {
        val order = listOf(current, YearTerm.ALL_YEAR, if (current == YearTerm.FIRST) YearTerm.SECOND else YearTerm.FIRST)
        return YearTab(
            area = area, label = label, done = views.count { it.done }, total = views.size,
            sections = order.mapNotNull { term -> views.filter { it.task.term == term }.takeIf { it.isNotEmpty() }?.let { term to it } },
        )
    }

    private companion object {
        const val ALL_LABEL = "전체"
    }
}
