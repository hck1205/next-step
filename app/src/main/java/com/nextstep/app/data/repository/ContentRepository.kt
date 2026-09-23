package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.ContentEntity
import kotlinx.coroutines.flow.Flow

/** 교육 콘텐츠(유튜브 링크) 저장소. 가족 저장소와 공용 저장소를 함께 읽습니다. */
interface ContentRepository {
    val contents: Flow<List<ContentEntity>>
    /** 링크를 분석해 초안을 만듭니다. 저장하지 않습니다. */
    suspend fun prepare(url: String): Result<ContentDraft>
    suspend fun save(content: ContentEntity)
    suspend fun rate(id: String, stars: Int)
    suspend fun setWatched(id: String, watched: Boolean)
    suspend fun delete(id: String)
}
