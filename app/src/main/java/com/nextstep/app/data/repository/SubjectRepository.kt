package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
    val subjects: Flow<List<SubjectEntity>>
    fun observe(id: String): Flow<SubjectEntity?>
    suspend fun save(subject: SubjectEntity)
    /** 과목과 그 단원을 함께 소프트 삭제합니다. */
    suspend fun delete(id: String)
}
