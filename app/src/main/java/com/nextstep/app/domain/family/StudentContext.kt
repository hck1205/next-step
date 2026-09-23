package com.nextstep.app.domain.family

import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.JourneyPeriod
import com.nextstep.app.domain.journey.PeriodCalendar
import java.time.LocalDate

/**
 * 구성원 목록에서 학생 한 명의 시간 맥락을 한 번에 뽑습니다: 생년월일, 성장 단계, 구간 달력, 현재 구간.
 * 화면마다 반복되던 계산을 모아 두어 ViewModel 은 이것 하나만 만들면 됩니다.
 */
data class StudentContext(
    val student: MemberEntity?,
    val birthDate: LocalDate?,
    val stage: GrowthStage?,
    val periods: List<JourneyPeriod>,
    val currentPeriod: JourneyPeriod?,
) {
    val hasBirthDate: Boolean get() = birthDate != null
    val currentPeriodKey: String? get() = currentPeriod?.key
    val gradeLabel: String? get() = student?.gradeYear?.takeIf { it > 0 }?.let { y -> GrowthStage.fromGradeYear(y)?.gradeLabel(y) }

    companion object {
        fun of(members: List<MemberEntity>, today: LocalDate): StudentContext {
            val student = members.firstOrNull { it.isStudent }
            val birthDate = student?.birthDate?.let { LocalDate.ofEpochDay(it) }
            val periods = birthDate?.let { PeriodCalendar.periods(it) }.orEmpty()
            return StudentContext(
                student = student,
                birthDate = birthDate,
                stage = GrowthStage.of(members, today),
                periods = periods,
                currentPeriod = PeriodCalendar.periodOf(periods, today),
            )
        }
    }
}
