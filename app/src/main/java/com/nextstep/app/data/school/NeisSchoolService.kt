package com.nextstep.app.data.school

import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.repository.FamilyEventRepository
import com.nextstep.app.domain.school.School
import com.nextstep.app.domain.school.SchoolCalendar
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/** NEIS 로 학교를 찾고, 학생 구성원의 학교 학사일정을 가족 달력([familyEvents])에 넣습니다(이미 있는 id 는 건드리지 않음). */
class NeisSchoolService(
    private val api: NeisApi,
    private val members: Flow<List<MemberEntity>>,
    private val familyEvents: FamilyEventRepository,
) : SchoolService {
    override val available: Boolean get() = api.available

    override suspend fun search(name: String): Result<List<School>> = runCatching {
        if (name.isBlank() || !api.available) emptyList() else api.searchSchools(name)
    }

    override suspend fun syncNow(today: LocalDate): Result<Int> = runCatching {
        val student = members.first().firstOrNull { it.isStudent && !it.deleted } ?: return@runCatching 0
        val school = School.fromKey(student.schoolCode, student.schoolName) ?: return@runCatching 0
        if (!api.available) return@runCatching 0
        val (from, to) = SchoolCalendar.yearRange(today)
        val events = SchoolCalendar.toFamilyEvents(api.schedule(school, from, to), SchoolCalendar.gradeInSchool(student.gradeYear), student.id, school)
        familyEvents.addMissing(events)
    }
}
