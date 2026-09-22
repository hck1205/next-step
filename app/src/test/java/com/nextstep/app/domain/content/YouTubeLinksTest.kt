package com.nextstep.app.domain.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YouTubeLinksTest {
    private val id = "dQw4w9WgXcQ"

    @Test
    fun extractsIdFromAllSupportedShapes() {
        listOf(
            "https://www.youtube.com/watch?v=$id&t=10s", "https://youtu.be/$id", "https://youtube.com/shorts/$id",
            "https://www.youtube.com/embed/$id", "https://www.youtube.com/live/$id?feature=share", "  https://youtu.be/$id  ",
            "https://m.youtube.com/watch?feature=x&v=$id",
        ).forEach { assertEquals(it, id, YouTubeLinks.videoId(it)) }
    }

    @Test
    fun rejectsNonYouTubeAndMalformedLinks() {
        assertNull(YouTubeLinks.videoId("https://example.com/watch?v=$id"))
        assertNull(YouTubeLinks.videoId("https://youtu.be/short"))
        assertNull(YouTubeLinks.videoId(""))
        assertFalse(YouTubeLinks.isYouTube("https://vimeo.com/123"))
        assertTrue(YouTubeLinks.isYouTube("https://youtu.be/$id"))
    }

    @Test
    fun canonicalAndThumbnailUrlsUseTheId() {
        assertEquals("https://www.youtube.com/watch?v=$id", YouTubeLinks.canonicalUrl(id))
        assertEquals("https://img.youtube.com/vi/$id/hqdefault.jpg", YouTubeLinks.thumbnailUrl(id))
    }
}
