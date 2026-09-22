package com.nextstep.app.domain.content

/** 유튜브 URL 에서 영상 ID 를 추출합니다. 지원: watch?v=, youtu.be/, shorts/, embed/, live/ */
object YouTubeLinks {
    private val patterns = listOf(
        Regex("""[?&]v=([A-Za-z0-9_-]{11})"""),
        Regex("""youtu\.be/([A-Za-z0-9_-]{11})"""),
        Regex("""/shorts/([A-Za-z0-9_-]{11})"""),
        Regex("""/embed/([A-Za-z0-9_-]{11})"""),
        Regex("""/live/([A-Za-z0-9_-]{11})"""),
    )

    fun videoId(url: String): String? {
        val trimmed = url.trim()
        if (!trimmed.contains("youtu", ignoreCase = true)) return null
        return patterns.firstNotNullOfOrNull { it.find(trimmed)?.groupValues?.get(1) }
    }

    fun isYouTube(url: String): Boolean = videoId(url) != null

    fun canonicalUrl(videoId: String): String = "https://www.youtube.com/watch?v=$videoId"
    fun thumbnailUrl(videoId: String): String = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
}
