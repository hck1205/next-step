package com.nextstep.app.domain.journey

import com.nextstep.app.domain.growth.GrowthStage
import java.time.LocalDate

/**
 * 타임라인의 한 구간. 영아는 3개월, 유아·유치원기는 6개월, 학령기부터는 학기(1학기 3~8월, 2학기 9~2월) 단위입니다.
 * 목표는 한 번에 이룰 수 없으므로 이 구간 하나에 단계 하나씩을 배정해 진행합니다.
 *
 * [key] 는 저장된 목표 단계와 연결되므로 형식을 바꾸지 않습니다: 나이 구간 "age-{시작개월}", 학기 "g{학년}s{학기}".
 */
data class JourneyPeriod(
    val key: String,
    val label: String,
    val stage: GrowthStage,
    val start: LocalDate,
    /** 마지막 날(포함). */
    val end: LocalDate,
    val gradeYear: Int? = null,
    val semester: Int? = null,
) {
    val isSchoolTerm: Boolean get() = gradeYear != null
    operator fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)

    companion object {
        fun ageKey(startMonths: Int) = "age-$startMonths"
        fun termKey(gradeYear: Int, semester: Int) = "g${gradeYear}s$semester"
    }
}
