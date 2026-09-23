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

    /** 저장소에 맞는 영상이 없을 때 여는 유튜브 검색. 검색어는 "학년 과목 단원 개념" 꼴이 결과가 가장 좋습니다. */
    fun searchUrl(query: String): String = "https://www.youtube.com/results?search_query=" + java.net.URLEncoder.encode(query.trim(), "UTF-8")
    fun thumbnailUrl(videoId: String): String = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
}
