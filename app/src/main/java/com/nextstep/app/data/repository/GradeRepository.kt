package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.GradeEntity
import kotlinx.coroutines.flow.Flow

interface GradeRepository {
    val grades: Flow<List<GradeEntity>>
    suspend fun save(grade: GradeEntity)
    suspend fun delete(id: String)
}
