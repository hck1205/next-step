package com.nextstep.app.domain.familytalk

import com.nextstep.app.data.local.entity.CheerEntity
import com.nextstep.app.data.local.entity.FamilyEventEntity
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.domain.familycalendar.FamilyCalendar
import com.nextstep.app.domain.familycalendar.FamilyEventKind
import com.nextstep.app.domain.familycalendar.FamilyOccurrence
import com.nextstep.app.domain.feedback.FeedbackAudience
import com.nextstep.app.domain.feedback.FeedbackVoice
import com.nextstep.app.domain.feedback.Finding
import com.nextstep.app.domain.time.DateUtils
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * 주말 이야기: 한 주를 "반짝인 순간"으로 닫고 다음 주를 "기대되는 것"으로 여는 가족의 10분.
 * 부담이 아니라 기대가 되도록 — 좋았던 것만 모으고(밀린 것·점수·비교 없음), 다음 주는 해야 할 일이 아니라 해 보고 싶은 것과 함께 할 즐거움으로 적습니다.
 * 이야기는 그 주의 주간 계획 행(WeekPlanEntity.proud · wish · treat · talkAt)에 남습니다.
 */
object FamilyTalk {

    /** 이야기할 주: 금~일이면 이번 주, 월~목이면 지난주. */
    fun talkWeek(today: LocalDate): LocalDate =
        DateUtils.weekStart(today).let { if (today.dayOfWeek >= TALK_FROM) it else it.minusWeeks(1) }

    /** [week] 주(월~일)의 반짝인 순간. [findings] 는 그 주 피드백의 사실(잘한 것만 아이의 말로 옮깁니다). */
    fun highlights(
        week: LocalDate, tasks: List<TaskEntity>, sessions: List<StudySessionEntity>, cheers: List<CheerEntity>,
        goals: List<GoalEntity>, findings: List<Finding>, numbers: Boolean,
    ): WeekHighlights {
        val days = week..week.plusDays(DAYS_IN_WEEK - 1)
        fun inWeek(millis: Long?) = millis?.let { DateUtils.toLocalDate(it) in days } == true
        return WeekHighlights(
            doneTasks = tasks.count { !it.deleted && it.done && inWeek(it.doneAt) },
            studyDays = sessions.filter { !it.deleted }.map { DateUtils.toLocalDate(it.startAt) }.filter { it in days }.toSet().size,
            cheers = cheers.count { !it.deleted && inWeek(it.createdAt) },
            goalsDone = goals.filter { !it.deleted && it.status == GoalStatus.DONE && inWeek(it.doneAt) }.map { it.title },
            sparkles = findings.filter { it.kind.good }.map { FeedbackVoice.line(it, FeedbackAudience.STUDENT, numbers).title },
        )
    }

    /** 반짝인 순간을 한 줄씩(좋았던 것만). 아무것도 없으면 빈 목록 — 화면은 "쉬어 간 주"라고 다독입니다. */
    fun highlightLines(h: WeekHighlights): List<String> = listOfNotNull(
        h.doneTasks.takeIf { it > 0 }?.let { "✅ 해낸 일 ${it}개" },
        h.studyDays.takeIf { it > 0 }?.let { "📚 공부한 날 ${it}일" },
        h.cheers.takeIf { it > 0 }?.let { "💛 받은 응원 ${it}개" },
    ) + h.goalsDone.map { "🏆 $it 이뤘어요" } + h.sparkles.map { "✨ $it" }

    /** "가장 자랑하고 싶은 것" 고르기 칩: 그 주에 해낸 일과 이룬 목표(새것부터 [MAX_IDEAS] 개). */
    fun proudIdeas(week: LocalDate, tasks: List<TaskEntity>, highlights: WeekHighlights): List<String> {
        val days = week..week.plusDays(DAYS_IN_WEEK - 1)
        val done = tasks.filter { t -> !t.deleted && t.done && t.doneAt?.let { DateUtils.toLocalDate(it) in days } == true }.sortedByDescending { it.doneAt }.map { it.title }
        return (highlights.goalsDone + done).distinct().take(MAX_IDEAS)
    }

    /** 다음 주(월~일)에 기대되는 가족 일정: 즐거운 종류(나들이·기념일·가족·약속)가 먼저, 회차마다 첫날만. */
    fun lookForward(events: List<FamilyEventEntity>, week: LocalDate): List<FamilyOccurrence> {
        val next = week.plusWeeks(1)
        return (0L until DAYS_IN_WEEK).flatMap { FamilyCalendar.on(events, next.plusDays(it)) }.filter { it.isFirstDay }
            .distinctBy { it.event.id to it.start }
            .sortedWith(compareBy<FamilyOccurrence>({ it.kind !in FUN }, { it.start }))
            .take(MAX_IDEAS)
    }

    /** 오늘 화면 카드: 금~일에 아직 이야기하지 않았으면 초대, 나눈 이야기에 기대되는 것이 있으면 그것(다음 주 목요일까지). */
    fun card(today: LocalDate, plans: List<WeekPlanEntity>): TalkCard {
        val week = talkWeek(today)
        val talk = plans.firstOrNull { !it.deleted && it.weekStart == week.toEpochDay() }?.takeIf { it.talkAt != null }
        return when {
            talk != null && (talk.wish.isNotBlank() || talk.treat.isNotBlank()) -> TalkCard(TalkPhase.LOOKING_FORWARD, talk.wish, talk.treat)
            talk == null && today.dayOfWeek >= TALK_FROM -> TalkCard(TalkPhase.INVITE)
            else -> TalkCard(TalkPhase.NONE)
        }
    }

    /** [week] 주의 이야기를 이미 나눴는지. */
    fun talked(plans: List<WeekPlanEntity>, week: LocalDate): Boolean = plans.any { !it.deleted && it.weekStart == week.toEpochDay() && it.talkAt != null }

    /** 해 보고 싶은 것(해야 할 일이 아니라 기대) 고르기 칩. */
    val WISH_IDEAS = listOf("새 책 한 권 읽기", "자전거 타기", "요리 하나 같이 만들기", "좋아하는 노래 연습", "새 보드게임 배우기", "그림 한 장 완성하기")

    /** 가족이 함께 할 작은 즐거움 칩. */
    val TREAT_IDEAS = listOf("보드게임 밤", "같이 산책", "영화 보는 저녁", "공원 소풍", "같이 요리하기", "잠들기 전 책 읽어 주기")

    /** 이야기를 여는 요일(금요일부터 일요일까지). */
    val TALK_FROM: DayOfWeek = DayOfWeek.FRIDAY
    private val FUN = setOf(FamilyEventKind.OUTING, FamilyEventKind.CELEBRATION, FamilyEventKind.FAMILY, FamilyEventKind.PROMISE)
    private const val DAYS_IN_WEEK = 7L
    private const val MAX_IDEAS = 6
}
