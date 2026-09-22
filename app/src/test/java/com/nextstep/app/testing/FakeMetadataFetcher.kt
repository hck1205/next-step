package com.nextstep.app.testing

import com.nextstep.app.data.remote.VideoMetadata
import com.nextstep.app.data.remote.VideoMetadataFetcher

class FakeMetadataFetcher(private val result: VideoMetadata?) : VideoMetadataFetcher {
    var calls = 0
    override suspend fun fetch(videoUrl: String): VideoMetadata? { calls++; return result }
}
