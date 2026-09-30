package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.LessonEntity
import com.nextstep.app.domain.lesson.LessonStatus
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/** 수업 출결 기록. 적는 멘토는 현재 프로필입니다(멘토 한 명의 하루에 한 행). */
interface LessonRepository {
    val lessons: Flow<List<LessonEntity>>
    /** 그날 출결을 적거나 바꿉니다. [status] 가 null 이면 기록을 지웁니다. */
    suspend fun mark(date: LocalDate, status: LessonStatus?, note: String = "")
}
