package com.nextstep.app.fake

import com.nextstep.app.data.school.SchoolService
import com.nextstep.app.domain.school.School
import java.time.LocalDate

class FakeSchoolService(override val available: Boolean = true, var found: List<School> = emptyList(), var synced: Result<Int> = Result.success(0)) : SchoolService {
    val calls = mutableListOf<String>()
    override suspend fun search(name: String): Result<List<School>> { calls += "search:$name"; return Result.success(found) }
    override suspend fun syncNow(today: LocalDate): Result<Int> { calls += "sync"; return synced }
}
