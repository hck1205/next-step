package com.nextstep.app.ui.growthalbum.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstep.app.domain.album.GrowthAlbum
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.oneDecimal
import com.nextstep.app.ui.theme.Indigo
import com.nextstep.app.ui.theme.IndigoDark
import com.nextstep.app.ui.theme.IndigoDeep
import com.nextstep.app.ui.theme.IndigoLight
import com.nextstep.app.ui.theme.IndigoNight
import com.nextstep.app.ui.theme.isDark
import com.nextstep.app.ui.theme.storyStyle

/** 앨범 표지: 짙은 두 빛깔 바탕 · 아이 이름 첫 글자 · 큰 글씨 "지우의 2029학년도" · 기간 · 한 해 숫자 네 칸(이룬 목표 · 공부한 날 · 해 본 것 · 자란 키). */
@Composable
internal fun AlbumCover(book: GrowthAlbum) {
    val dark = MaterialTheme.colorScheme.isDark
    val (top, bottom, ink) = if (dark) Triple(IndigoDark, IndigoNight, IndigoLight) else Triple(Indigo, IndigoDeep, Color.White)
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Brush.linearGradient(listOf(top, bottom))).padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Filled.AutoStories, contentDescription = null, tint = ink.copy(alpha = SUB), modifier = Modifier.size(16.dp))
            Text("성장 앨범", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = ink.copy(alpha = SUB))
        }
        Box(Modifier.padding(top = 8.dp).size(60.dp).background(ink, CircleShape), contentAlignment = Alignment.Center) {
            Text(book.studentName.take(1), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = top)
        }
        Text("${book.studentName}의\n${book.year.label}", style = storyStyle(30.sp), color = ink, modifier = Modifier.semantics { heading() })
        Text("${DateUtils.formatMonth(book.year.start)} – ${DateUtils.formatMonth(book.year.end)} · 좋았던 것만 모았어요", style = MaterialTheme.typography.bodySmall, color = ink.copy(alpha = SUB))
        Box(Modifier.padding(top = 10.dp).fillMaxWidth().height(1.dp).background(ink.copy(alpha = LINE)))
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            val cells = listOf(
                "${book.goals.size}" to "이룬 목표", "${book.studyDays}" to "공부한 날", "${book.activities.size}" to "해 본 것",
                (book.height?.let { "+${it.gainCm.oneDecimal()}" } ?: "–") to "자란 키 cm",
            )
            cells.forEachIndexed { i, (v, l) ->
                if (i > 0) Box(Modifier.width(1.dp).height(34.dp).background(ink.copy(alpha = LINE)))
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(v, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = ink, textAlign = TextAlign.Center)
                    Text(l, style = MaterialTheme.typography.labelSmall, color = ink.copy(alpha = SUB), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

private const val SUB = 0.75f
private const val LINE = 0.3f
