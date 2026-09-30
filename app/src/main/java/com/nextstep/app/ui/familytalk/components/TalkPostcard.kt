package com.nextstep.app.ui.familytalk.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.domain.familycalendar.FamilyOccurrence
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.PostcardFrame
import com.nextstep.app.ui.components.card.PostcardLine
import com.nextstep.app.ui.theme.handStyle
import java.time.LocalDate

/**
 * 나눈 이야기를 "우리 가족 다음 주 카드" 엽서로: 우표(다음 주) · 자랑 · 해 보고 싶은 것 · 가족 즐거움(손글씨) · 기다리는 일 입장권.
 * [onEdit] 이 있으면 "다시 이야기하기".
 */
@Composable
internal fun TalkPostcard(saved: WeekPlanEntity, week: LocalDate, next: FamilyOccurrence?, today: LocalDate, onEdit: (() -> Unit)?) {
    val cs = MaterialTheme.colorScheme
    PostcardFrame(corner = { Stamp(week.plusWeeks(1)) }) {
        Text("우리 가족\n다음 주 카드", style = handStyle(38.sp), color = cs.onSurface, modifier = Modifier.padding(end = 76.dp))
        if (saved.proud.isNotBlank()) PostcardLine("이번 주 자랑", Icons.Filled.EmojiEvents, saved.proud)
        if (saved.wish.isNotBlank()) PostcardLine("해 보고 싶은 것", Icons.Filled.Eco, saved.wish)
        if (saved.treat.isNotBlank()) PostcardLine("가족 즐거움", Icons.Filled.Celebration, saved.treat)
        if (saved.proud.isBlank() && saved.wish.isBlank() && saved.treat.isBlank()) Text("이번 주 이야기를 나눴어요", style = handStyle(28.sp), color = cs.onSurface)
        next?.let { TalkTicket(it, today) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${DateUtils.formatShortDate(week)} – ${DateUtils.formatShortDate(week.plusDays(LAST_DAY))} 주에 나눈 이야기",
                style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f),
            )
            if (onEdit != null) TextButton(onClick = onEdit) { Text("다시 이야기하기") }
        }
    }
}

@Composable
private fun Stamp(nextWeek: LocalDate) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(8.dp)
    Column(
        Modifier.rotate(STAMP_TILT).background(cs.secondaryContainer, shape).border(2.dp, cs.secondary, shape).padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Icon(Icons.Filled.Mail, contentDescription = null, tint = cs.secondary, modifier = Modifier.size(18.dp))
        Text(DateUtils.formatShortDate(nextWeek), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = cs.onSecondaryContainer)
        Text("주", style = MaterialTheme.typography.labelSmall, color = cs.onSecondaryContainer)
    }
}

private const val LAST_DAY = 6L
private const val STAMP_TILT = 5f
