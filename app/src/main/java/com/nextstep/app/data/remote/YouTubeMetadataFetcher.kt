package com.nextstep.app.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * 유튜브 oEmbed 엔드포인트로 제목·채널·썸네일을 가져옵니다. API 키가 필요 없고 공개 영상이면 동작합니다.
 * 실패해도 등록은 가능해야 하므로 null 을 돌려주고 사용자가 직접 입력하게 합니다.
 */
class YouTubeMetadataFetcher : VideoMetadataFetcher {
    override suspend fun fetch(videoUrl: String): VideoMetadata? = withContext(Dispatchers.IO) {
        try {
            val endpoint = "https://www.youtube.com/oembed?format=json&url=" + URLEncoder.encode(videoUrl, "UTF-8")
            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                requestMethod = "GET"
            }
            try {
                if (conn.responseCode != 200) return@withContext null
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                VideoMetadata(
                    title = json.optString("title"),
                    channel = json.optString("author_name"),
                    thumbnailUrl = json.optString("thumbnail_url"),
                )
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.w("YouTubeMetadata", "oEmbed fetch failed", e)
            null
        }
    }

    private companion object {
        const val TIMEOUT_MS = 8_000
    }
}
