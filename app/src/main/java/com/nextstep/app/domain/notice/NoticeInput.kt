package com.nextstep.app.domain.notice

import com.nextstep.app.domain.familycalendar.FamilyOccurrence
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.domain.lesson.TuitionDue
import com.nextstep.app.domain.mission.MissionFocus
import com.nextstep.app.domain.stats.UpcomingExam

/**
 * 알림을 만들 재료 한 벌(이 기기 사용자가 보는 범위 그대로 — 멘토면 담당 과목만, 가족 일정 없음).
 * [family] 는 가족(학생·학부모)인지, [isStudent] 는 학생 말투로 쓸지.
 */
data class NoticeInput(
    val family: Boolean,
    val isStudent: Boolean,
    val familyAhead: List<FamilyOccurrence> = emptyList(),
    /** 다음 주(월~일)의 가족 일정: 주말 이야기의 "기대되는 일". */
    val nextWeek: List<FamilyOccurrence> = emptyList(),
    val exams: List<UpcomingExam> = emptyList(),
    val missions: List<MissionFocus> = emptyList(),
    val journey: List<JourneyItem> = emptyList(),
    val dueToday: Int = 0,
    val overdue: Int = 0,
    /** 멘토: 오늘 수업의 시작 시각(분). 없으면 null. */
    val lessonAt: Int? = null,
    /** 다가오는 수업료(멘토는 받을 날, 학부모는 낼 날). 학생에게는 비어 있음. */
    val tuition: List<TuitionDue> = emptyList(),
    /** 이번 주 주말 이야기를 이미 나눴는지. */
    val talkDone: Boolean = false,
)
