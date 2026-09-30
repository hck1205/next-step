package com.nextstep.app.ui.familytalk.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.familytalk.WeekHighlights
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.StickerTile
import com.nextstep.app.ui.components.card.StoryHero

/** 1걸음 반짝인 순간: 좋았던 숫자를 스티커로(0 은 빼고), 이룬 목표·잘한 것은 리본 한 줄씩. 없으면 "쉬어 간 주"라고 다독입니다. */
@Composable
internal fun SparkleStep(highlights: WeekHighlights, weekLabel: String) {
    val cs = MaterialTheme.colorScheme
    val stickers = listOf(
        Sticker(Icons.Filled.CheckCircle, highlights.doneTasks, "개", "해낸 일", cs.primary, cs.primaryContainer),
        Sticker(Icons.AutoMirrored.Filled.MenuBook, highlights.studyDays, "일", "공부한 날", cs.secondary, cs.secondaryContainer),
        Sticker(Icons.Filled.Favorite, highlights.cheers, "개", "받은 응원", cs.error, cs.errorContainer),
        Sticker(Icons.Filled.EmojiEvents, highlights.goalsDone.size, "개", "이룬 목표", cs.tertiary, cs.tertiaryContainer),
    ).filter { it.value > 0 }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StoryHero("이번 주 반짝인 순간", "$weekLabel · 좋았던 것만 모았어요", Icons.Filled.AutoAwesome)
        if (stickers.isEmpty()) AppCard { Text("쉬어 간 주도 괜찮아요. 쉬는 것도 다음 주를 위한 힘이 돼요.", style = MaterialTheme.typography.bodyLarge) }
        stickers.chunked(2).forEachIndexed { r, row ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 2.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEachIndexed { c, s -> StickerTile(s.icon, s.value.toString(), s.unit, s.label, s.tint, s.container, TILTS[(r * 2 + c) % TILTS.size], Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        (highlights.goalsDone.map { "$it 이뤘어요" } + highlights.sparkles).forEach { WinRibbon(it) }
    }
}

@Composable
private fun WinRibbon(text: String) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().background(cs.tertiaryContainer, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = cs.tertiary, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = cs.onTertiaryContainer)
    }
}

private val TILTS = listOf(-1.4f, 1.1f, 0.8f, -0.9f)
