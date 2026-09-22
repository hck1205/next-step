package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

/** 구성원 간 메모(격려, 피드백). 작성자는 현재 프로필에서 채웁니다. */
interface NoteRepository {
    val notes: Flow<List<NoteEntity>>
    suspend fun add(text: String)
    suspend fun delete(id: String)
}
