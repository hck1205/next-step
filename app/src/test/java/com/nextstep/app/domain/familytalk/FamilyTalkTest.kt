package com.nextstep.app.domain.familytalk

import com.nextstep.app.data.local.entity.CheerEntity
import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.domain.feedback.FeedbackKind
import com.nextstep.app.domain.feedback.Finding
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.testing.Fixtures
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyTalkTest {
    private val monday = LocalDate.of(2029, 3, 5)
    private fun at(day: LocalDate) = DateUtils.toMillis(day, LocalTime.NOON)

    @Test
    fun talkWeekIsThisWeekFromFridayElseLastWeek() {
        assertEquals(monday, FamilyTalk.talkWeek(monday.plusDays(4))) // 금
        assertEquals(monday, FamilyTalk.talkWeek(monday.plusDays(6))) // 일
        assertEquals(monday.minusWeeks(1), FamilyTalk.talkWeek(monday.plusDays(2))) // 수 → 지난주
    }

    @Test
    fun highlightsCollectOnlyGoodThingsOfThatWeek() {
        val tasks = listOf(
            Fixtures.task("분수", monday, done = true).copy(doneAt = at(monday.plusDays(1))),
            Fixtures.task("일기", monday, done = true).copy(doneAt = at(monday.plusDays(3))),
            Fixtures.task("지난주", monday, done = true).copy(doneAt = at(monday.minusDays(1))),
            Fixtures.task("밀림", monday.minusDays(3)),
        )
        val sessions = listOf(Fixtures.session("math", monday, LocalTime.of(9, 0), 30), Fixtures.session("math", monday, LocalTime.of(20, 0), 30), Fixtures.session("math", monday.plusDays(2), LocalTime.of(9, 0), 30))
        val cheers = listOf(CheerEntity(familyId = "fam", taskId = "t", taskTitle = "분수", kind = "CLAP", createdAt = at(monday.plusDays(1))))
        val goals = listOf(Fixtures.goal("구구단", status = GoalStatus.DONE).copy(doneAt = at(monday.plusDays(2))))
        val findings = listOf(Finding(FeedbackKind.STUDY_STEADY, 4, 4), Finding(FeedbackKind.TASKS_OVERDUE, 3))
        val h = FamilyTalk.highlights(monday, tasks, sessions, cheers, goals, findings, numbers = true)
        assertEquals(2, h.doneTasks); assertEquals(2, h.studyDays); assertEquals(1, h.cheers); assertEquals(listOf("구구단"), h.goalsDone)
        assertEquals(listOf("꾸준히 하고 있어요"), h.sparkles) // 밀린 것은 모으지 않음
        assertEquals(listOf("✅ 해낸 일 2개", "📚 공부한 날 2일", "💛 받은 응원 1개", "🏆 구구단 이뤘어요", "✨ 꾸준히 하고 있어요"), FamilyTalk.highlightLines(h))
        assertEquals(listOf("구구단", "일기", "분수"), FamilyTalk.proudIdeas(monday, tasks, h))
        assertTrue(FamilyTalk.highlightLines(WeekHighlights()).isEmpty()); assertTrue(WeekHighlights().isEmpty)
    }

    @Test
    fun lookForwardPutsFunFirstAndCardFollowsTheWeek() {
        val next = monday.plusWeeks(1)
        val events = listOf(
            Fixtures.familyEvent("치과", next), Fixtures.familyEvent("캠핑", next.plusDays(5), kind = "OUTING").copy(kind = "OUTING"),
            Fixtures.familyEvent("치과2", next.plusDays(1), kind = "HOSPITAL"),
        )
        assertEquals(listOf("치과", "캠핑", "치과2"), FamilyTalk.lookForward(events, monday).map { it.event.title })
        val talked = WeekPlanEntity(familyId = "fam", weekStart = monday.toEpochDay(), wish = "자전거", treat = "보드게임 밤", talkAt = 1L)
        assertEquals(TalkPhase.INVITE, FamilyTalk.card(monday.plusDays(5), emptyList()).phase)
        assertEquals(TalkPhase.NONE, FamilyTalk.card(monday.plusDays(2), emptyList()).phase) // 평일 · 나눈 이야기 없음
        assertEquals(TalkCard(TalkPhase.LOOKING_FORWARD, "자전거", "보드게임 밤"), FamilyTalk.card(monday.plusDays(6), listOf(talked)))
        assertEquals(TalkPhase.LOOKING_FORWARD, FamilyTalk.card(next.plusDays(3), listOf(talked)).phase) // 다음 주 목요일까지
        assertEquals(TalkPhase.INVITE, FamilyTalk.card(next.plusDays(4), listOf(talked)).phase) // 다음 주 금요일엔 새 이야기
        assertTrue(FamilyTalk.talked(listOf(talked), monday)); assertFalse(FamilyTalk.talked(listOf(talked.copy(talkAt = null)), monday))
    }

    @Test
    fun pastKeepsOnlyEarlierWeeksWithSomethingSaid() {
        fun talk(weeksBack: Long, proud: String = "", wish: String = "", at: Long? = 1L) =
            WeekPlanEntity(familyId = "fam", weekStart = monday.minusWeeks(weeksBack).toEpochDay(), proud = proud, wish = wish, talkAt = at)
        val plans = listOf(
            talk(0, proud = "이번 주"), talk(1, proud = "줄넘기"), talk(2, wish = "자전거"), talk(3), talk(4, proud = "안 나눔", at = null),
            talk(5, proud = "5"), talk(6, proud = "6"), talk(7, proud = "7"),
        )
        assertEquals(listOf("줄넘기", "자전거", "5", "6"), FamilyTalk.past(plans, monday).map { it.proud.ifBlank { it.wish } })
    }
}
