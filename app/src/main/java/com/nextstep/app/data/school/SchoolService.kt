package com.nextstep.app.data.school

import com.nextstep.app.domain.school.School
import java.time.LocalDate

/** 가족 탭의 "학교": 학교 찾기와, 고른 학교의 학사일정을 가족 달력으로 받아오기. */
interface SchoolService {
    /** 인증키가 있어 쓸 수 있는지. */
    val available: Boolean
    suspend fun search(name: String): Result<List<School>>
    /** 학생 구성원에 고른 학교의 이번 학년도 학사일정을 받아 가족 달력에 새로 생긴 것만 넣습니다. 넣은 수. */
    suspend fun syncNow(today: LocalDate): Result<Int>
}
