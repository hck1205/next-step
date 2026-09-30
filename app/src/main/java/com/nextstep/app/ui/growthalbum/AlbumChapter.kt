package com.nextstep.app.ui.growthalbum

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector
import com.nextstep.app.domain.album.GrowthAlbum

/** 성장 앨범의 장(순서가 곧 앨범 순서). 기록이 있는 장만 펼칩니다(꾸준함은 공부한 날이 있을 때). */
enum class AlbumChapter(val title: String, val icon: ImageVector) {
    GOALS("해낸 것", Icons.Filled.WorkspacePremium),
    STEADY("꾸준함", Icons.Filled.LocalFireDepartment),
    TRIED("해 본 것", Icons.Filled.PhotoCamera),
    GREW("자란 것", Icons.Filled.Straighten),
    CHEERS("받은 응원", Icons.Filled.Favorite),
    TALKS("나눈 이야기", Icons.Filled.FormatQuote);

    fun present(book: GrowthAlbum): Boolean = when (this) {
        GOALS -> book.goals.isNotEmpty()
        STEADY -> book.studyDays > 0
        TRIED -> book.activities.isNotEmpty()
        GREW -> book.height != null
        CHEERS -> book.cheers.isNotEmpty()
        TALKS -> book.talks.isNotEmpty()
    }
}
