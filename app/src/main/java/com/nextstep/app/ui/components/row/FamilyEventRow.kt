package com.nextstep.app.ui.components.row

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.domain.familycalendar.FamilyCalendar
import com.nextstep.app.domain.familycalendar.FamilyOccurrence
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.icon.familyKindColor
import com.nextstep.app.ui.components.icon.familyKindIcon

/**
 * 가족 일정 한 줄: 종류 아이콘 · 제목 · 때와 누구 · (있으면) 장소 · 챙기는 사람 · 준비물.
 * [dayLabel] 이 있으면 제목 앞에 날 이름(오늘 화면의 "내일" 등). [onClick] 이 있으면 눌러 고칩니다.
 */
@Composable
fun FamilyEventRow(occ: FamilyOccurrence, members: List<MemberEntity>, onClick: (() -> Unit)? = null, dayLabel: String? = null) {
    val e = occ.event
    val extra = listOfNotNull(
        e.location.takeIf { it.isNotBlank() },
        FamilyCalendar.keeper(e, members)?.let { "$it 챙김" },
        e.bringList.takeIf { it.isNotEmpty() }?.let { "준비물 ${it.joinToString(", ")}" },
    ).joinToString(" · ")
    AppCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KindBadge(familyKindColor(occ.kind)) { Icon(familyKindIcon(occ.kind), contentDescription = occ.kind.label, tint = Color.White, modifier = Modifier.size(20.dp)) }
            Column(Modifier.weight(1f)) {
                Text(listOfNotNull(dayLabel, e.title).joinToString(" · "), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${FamilyCalendar.timeLabel(occ)} · ${FamilyCalendar.who(e, members)}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (extra.isNotEmpty()) Text(extra, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun KindBadge(color: Color, content: @Composable () -> Unit) {
    Box(Modifier.size(36.dp).background(color, CircleShape), contentAlignment = Alignment.Center) { content() }
}
