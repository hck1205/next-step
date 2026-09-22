package com.nextstep.app.data.remote

data class VideoMetadata(val title: String, val channel: String, val thumbnailUrl: String)

/** 영상 링크의 제목·채널·썸네일을 가져오는 경계. 실패하면 null (등록은 계속 가능해야 합니다). */
fun interface VideoMetadataFetcher {
    suspend fun fetch(videoUrl: String): VideoMetadata?
}
