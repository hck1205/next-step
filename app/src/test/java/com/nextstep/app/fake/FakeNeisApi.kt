package com.nextstep.app.fake

import com.nextstep.app.data.school.NeisApi
import com.nextstep.app.domain.school.School
import com.nextstep.app.domain.school.SchoolDay
import java.time.LocalDate

class FakeNeisApi(override val available: Boolean = true, var schools: List<School> = emptyList(), var days: List<SchoolDay> = emptyList()) : NeisApi {
    val asked = mutableListOf<String>()
    override suspend fun searchSchools(name: String): List<School> { asked += "search:$name"; return schools }
    override suspend fun schedule(school: School, from: LocalDate, to: LocalDate): List<SchoolDay> { asked += "schedule:${school.key}:$from:$to"; return days }
}
