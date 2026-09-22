package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.domain.content.ContentClassification
import kotlinx.coroutines.flow.Flow

/** 링크 등록 화면에 채워 넣을 초안. 메타데이터와 자동 분류 결과를 담습니다. */
data class ContentDraft(
    val url: String,
    val videoId: String,
    val title: String,
    val channel: String,
    val thumbnailUrl: String,
    val classification: ContentClassification,
    val metadataFetched: Boolean,
)

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
