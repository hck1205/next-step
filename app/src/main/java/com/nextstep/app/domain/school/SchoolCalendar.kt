package com.nextstep.app.domain.school

import com.nextstep.app.data.local.entity.FamilyEventEntity
import com.nextstep.app.domain.familycalendar.FamilyEventKind
import com.nextstep.app.domain.familycalendar.FamilyHeadsUp
import com.nextstep.app.domain.familycalendar.FamilyRepeat
import com.nextstep.app.domain.time.SchoolYear
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * NEIS 학사일정 → 가족 달력 일정. 사람이 적지 않아도 방학 · 재량휴업일 · 시험 · 행사가 "학교" 일정으로 들어옵니다.
 * 우리 아이 학년이 아닌 행사와 매주 반복되는 토요휴업일은 뺍니다. 같은 이름이 이어지는 날(주말 건너뜀 포함)은 여러 날 일정 하나로 묶습니다.
 * 일정 id 는 학교·날짜·이름으로 정해져 있어 여러 번 받아도 한 번만 들어가고, 가족이 고치거나 지운 것은 다시 덮지 않습니다(저장소가 없는 것만 넣음).
 */
object SchoolCalendar {

    /** 학년도 범위: 3월 1일 ~ 다음 해 2월 말일. */
    fun yearRange(today: LocalDate): Pair<LocalDate, LocalDate> {
        val year = SchoolYear.of(today)
        return SchoolYear.start(year) to SchoolYear.end(year)
    }

    /** 학교 안의 학년(초 1~6 · 중 1~3 · 고 1~3). 학년을 모르면 0(모든 학년 행사를 받음). */
    fun gradeInSchool(gradeYear: Int): Int = when (gradeYear) {
        in 1..ELEMENTARY -> gradeYear
        in ELEMENTARY + 1..MIDDLE -> gradeYear - ELEMENTARY
        in MIDDLE + 1..HIGH -> gradeYear - MIDDLE
        else -> 0
    }

    fun toFamilyEvents(days: List<SchoolDay>, grade: Int, studentId: String, school: School): List<FamilyEventEntity> =
        merge(days.filter { keep(it, grade) }.sortedBy { it.date }).map { (first, last) -> event(first, last, studentId, school) }

    /** 이 학년에게 필요한 날인지: 이름이 있고, 토요휴업일이 아니고, 학년이 정해져 있으면 우리 학년이 들어 있는 것. */
    private fun keep(day: SchoolDay, grade: Int): Boolean =
        day.name.isNotBlank() && day.name !in SKIP && (grade == 0 || day.grades.isEmpty() || grade in day.grades)

    /** 같은 이름이 [MAX_GAP_DAYS] 일 안으로 이어지면 한 덩어리(첫날, 끝날). */
    private fun merge(days: List<SchoolDay>): List<Pair<SchoolDay, SchoolDay>> {
        val runs = mutableListOf<Pair<SchoolDay, SchoolDay>>()
        days.forEach { d ->
            val i = runs.indexOfLast { it.first.name == d.name && ChronoUnit.DAYS.between(it.second.date, d.date) in 0..MAX_GAP_DAYS }
            if (i >= 0) runs[i] = runs[i].first to d else runs += d to d
        }
        return runs
    }

    private fun event(first: SchoolDay, last: SchoolDay, studentId: String, school: School) = FamilyEventEntity(
        id = "neis-${school.key}-${first.date}-${first.name.hashCode().toUInt().toString(RADIX)}",
        familyId = "", title = first.name, kind = FamilyEventKind.SCHOOL.name,
        startDate = first.date.toEpochDay(), endDate = last.date.toEpochDay(), allDay = true,
        memberIds = studentId, repeat = FamilyRepeat.NONE.name, headsUp = headsUp(first).name,
        memo = "${school.name} 학사일정(NEIS)", createdByRole = SOURCE,
    )

    /** 시험은 일주일 전부터, 쉬는 날·방학은 3일 전부터(아이 맡길 곳 챙기기), 나머지는 전날부터. */
    private fun headsUp(day: SchoolDay): FamilyHeadsUp = when {
        EXAM_WORDS.any { it in day.name } -> FamilyHeadsUp.WEEK
        day.dayOff || OFF_WORDS.any { it in day.name } -> FamilyHeadsUp.THREE_DAYS
        else -> FamilyHeadsUp.DAY_BEFORE
    }

    /** NEIS 에서 받아 넣은 일정의 작성자 표시. */
    const val SOURCE = "NEIS"
    private const val ELEMENTARY = 6
    private const val MIDDLE = 9
    private const val HIGH = 12
    private const val MAX_GAP_DAYS = 3L
    private const val RADIX = 36
    private val SKIP = setOf("토요휴업일")
    private val EXAM_WORDS = listOf("고사", "평가", "시험")
    private val OFF_WORDS = listOf("방학", "휴업", "재량")
}
