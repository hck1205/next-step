package com.nextstep.app.data.school

import com.nextstep.app.domain.school.School
import com.nextstep.app.domain.school.SchoolDay
import java.time.LocalDate

/** NEIS 교육정보 개방 포털(open.neis.go.kr)에서 필요한 두 가지: 학교 찾기, 학사일정. */
interface NeisApi {
    /** 켜져 있는지(인증키가 있는지). 없으면 학교 일정 받기를 쓰지 않습니다. */
    val available: Boolean
    suspend fun searchSchools(name: String): List<School>
    suspend fun schedule(school: School, from: LocalDate, to: LocalDate): List<SchoolDay>
}
