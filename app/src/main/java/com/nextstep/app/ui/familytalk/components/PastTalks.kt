package com.nextstep.app.ui.familytalk.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.theme.handStyle

/** 지난 이야기: 옆으로 넘기는 작은 쪽지들(그 주 · 자랑이나 해 보고 싶은 것 · 가족 즐거움). */
@Composable
internal fun PastTalks(past: List<WeekPlanEntity>) {
    if (past.isEmpty()) return
    val cs = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("지난 이야기", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 4.dp, horizontal = 2.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            past.forEachIndexed { i, t ->
                val shape = RoundedCornerShape(14.dp)
                Column(
                    Modifier.width(160.dp).rotate(if (i % 2 == 0) -1f else 1f).background(cs.surface, shape).border(1.dp, cs.outlineVariant, shape).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("${DateUtils.formatShortDate(DateUtils.fromEpochDay(t.weekStart))} 주", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    Text(t.proud.ifBlank { t.wish }, style = handStyle(24.sp), color = cs.onSurface)
                    if (t.treat.isNotBlank()) Text(t.treat, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
            }
        }
    }
}
