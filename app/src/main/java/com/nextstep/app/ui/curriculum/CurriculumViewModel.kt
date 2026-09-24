package com.nextstep.app.ui.curriculum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.data.model.SubjectPalette
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.repository.ContentRepository
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.PeerCurriculumRepository
import com.nextstep.app.data.repository.SubjectRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.data.repository.TopicRepository
import com.nextstep.app.domain.curriculum.CurriculumCatalog
import com.nextstep.app.domain.curriculum.CurriculumRecommender
import com.nextstep.app.domain.curriculum.CurriculumUnit
import com.nextstep.app.domain.curriculum.UnitStatus
import com.nextstep.app.domain.family.StudentContext
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.JourneyPeriod
import com.nextstep.app.domain.journey.PeriodCalendar
import com.nextstep.app.domain.time.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.nextstep.app.ui.common.asUiState

/**
 * 학기별 교과 커리큘럼: 카탈로그를 가족 과목·단원·진도, 또래 통계, 콘텐츠 저장소와 대조해 보여 주고,
 * 단원을 내 과목으로 가져오거나 할 일로 보냅니다. 생년월일이 없으면 학년으로 학기를 추정합니다.
 */
class CurriculumViewModel(
    private val streams: FamilyDataStreams,
    private val peers: PeerCurriculumRepository,
    private val subjects: SubjectRepository,
    private val topics: TopicRepository,
    private val tasks: TaskRepository,
    private val contents: ContentRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    /** null 이면 현재 구간. */
    private val selectedKey = MutableStateFlow<String?>(null)

    private val base = combine(streams.profile, streams.members, selectedKey) { profile, members, selected ->
        val day = today()
        val ctx = StudentContext.of(members, day)
        val student = ctx.student
        val periods = periodsFor(ctx.birthDate, student?.gradeYear ?: 0, day)
        val currentKey = ctx.currentPeriodKey ?: student?.gradeYear?.takeIf { it > 0 }?.let { JourneyPeriod.termKey(it, semesterOf(day)) }
        val key = selected?.takeIf { k -> periods.any { it.key == k } } ?: currentKey ?: periods.firstOrNull()?.key
        CurriculumUiState(
            studentName = profile.studentName, hasBirthDate = ctx.hasBirthDate, periods = periods, currentPeriodKey = currentKey, selectedPeriodKey = key,
            loaded = true,
        ) to (ctx.stage?.gradeLevel ?: GradeLevel.ALL)
    }

    val state: StateFlow<CurriculumUiState> = combine(base, streams.subjects, streams.topics, peers.peerTopics, streams.contents) { (s, level), subjects, topics, peers, contents ->
        val curriculum = CurriculumCatalog.forPeriod(s.selectedPeriodKey)
        val plan = curriculum?.let { CurriculumRecommender.plan(it, subjects, topics, peers, contents, level, s.selected?.label ?: "") }
        val nextKey = s.periods.getOrNull(s.periods.indexOfFirst { it.key == s.selectedPeriodKey } + 1)?.key
        s.copy(plan = plan, nextPreview = CurriculumCatalog.forPeriod(nextKey)?.units?.filter { it.essential }?.map { "${it.subject} · ${it.title}" }.orEmpty())
    }.asUiState(viewModelScope, CurriculumUiState())

    fun prev() = move(-1)
    fun next() = move(1)
    fun thisPeriod() { selectedKey.value = null }

    /** 과목의 미등록 단원을 가족 과목·단원으로 복사합니다. 과목이 없으면 팔레트 색으로 새로 만듭니다. */
    fun importSubject(subjectName: String) = viewModelScope.launch {
        val plan = state.value.plan ?: return@launch
        val sp = plan.subjects.firstOrNull { it.subject == subjectName } ?: return@launch
        val titles = sp.notRegistered.map { it.title }
        if (titles.isEmpty()) return@launch
        val subjectId = sp.familySubject?.id ?: SubjectEntity(familyId = "", name = subjectName, color = SubjectPalette.colorFor(subjectName), orderIndex = plan.subjects.indexOf(sp)).also { subjects.save(it) }.id
        topics.add(subjectId, titles)
    }

    fun addTask(unit: CurriculumUnit, createdByRole: String) = viewModelScope.launch {
        val s = state.value
        val status = s.plan?.subjects?.flatMap { it.units }?.firstOrNull { it.unit == unit }?.status
        val type = if (status == UnitStatus.IN_CLASS) TaskType.REVIEW else TaskType.PREVIEW
        val subjectId = s.plan?.subjects?.firstOrNull { it.subject == unit.subject }?.familySubject?.id
        val due = s.selected?.end?.takeIf { !it.isBefore(today()) } ?: today().plusDays(DEFAULT_DUE_DAYS)
        tasks.save(TaskEntity(familyId = "", subjectId = subjectId, title = "${unit.subject} · ${unit.title}", type = type, dueDate = due.toEpochDay(), createdByRole = createdByRole, note = "이번 학기 커리큘럼"))
    }

    fun markWatched(contentId: String) = viewModelScope.launch { contents.setWatched(contentId, true) }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: CurriculumEvent) {
        when (event) {
            CurriculumEvent.PrevPeriod -> prev()
            CurriculumEvent.NextPeriod -> next()
            CurriculumEvent.ThisPeriod -> thisPeriod()
            is CurriculumEvent.ImportSubject -> importSubject(event.subject)
            is CurriculumEvent.AddTask -> addTask(event.unit, event.createdByRole)
            is CurriculumEvent.MarkWatched -> markWatched(event.contentId)
        }
    }

    /** 연속으로 눌러도 상태 재계산을 기다리지 않도록 선택 키 자체를 기준으로 움직입니다. */
    private fun move(delta: Int) {
        val s = state.value
        val from = selectedKey.value ?: s.selectedPeriodKey
        val index = s.periods.indexOfFirst { it.key == from }
        val target = s.periods.getOrNull(index + delta) ?: return
        selectedKey.value = target.key
    }

    /** 생년월일이 있으면 달력에서, 없으면 학년 기준 가상의 달력(입학 연도 역산)에서 학기 구간만 뽑습니다. */
    private fun periodsFor(birthDate: LocalDate?, gradeYear: Int, today: LocalDate): List<JourneyPeriod> {
        val base = birthDate ?: gradeYear.takeIf { it > 0 }?.let { g ->
            val entryYear = (if (today.monthValue >= SCHOOL_YEAR_START_MONTH) today.year else today.year - 1) - (g - 1)
            LocalDate.of(entryYear - GrowthStage.ELEMENTARY_ENTRY_YEARS_AFTER_BIRTH, 6, 1)
        } ?: return emptyList()
        return PeriodCalendar.periods(base).filter { CurriculumCatalog.forPeriod(it.key) != null }
    }

    private fun semesterOf(day: LocalDate): Int = if (day.monthValue in SCHOOL_YEAR_START_MONTH..FIRST_SEMESTER_END_MONTH) 1 else 2

    private companion object {
        const val SCHOOL_YEAR_START_MONTH = 3
        const val FIRST_SEMESTER_END_MONTH = 8
        const val DEFAULT_DUE_DAYS = 7L
    }
}
