package com.nextstep.app.ui.components.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.stats.HeatDay
import com.nextstep.app.domain.stats.HeatWeek
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/**
 * 공부 달력: 줄 = 주(월~일), 칸 = 하루, 진하기 = 공부한 양(남색 한 색, 적음 → 많음). 오늘은 테두리, 앞날은 빈 칸.
 * 칸을 누르면 그날 날짜와 시간이 아래에 나옵니다. [compact] 는 슬라이드용으로 칸을 낮게.
 */
@Composable
fun HeatCalendar(weeks: List<HeatWeek>, today: LocalDate, modifier: Modifier = Modifier, compact: Boolean = false) {
    val palette = ChartPalette.current()
    var picked by remember(weeks) { mutableStateOf<HeatDay?>(null) }
    val active = weeks.sumOf { it.activeDays }
    Column(modifier.semantics { contentDescription = "최근 ${weeks.size}주 공부 달력: ${active}일 공부" }, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(Modifier.width(LABEL_DP.dp))
            DAYS.forEach { d -> Text(d, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center) }
        }
        weeks.forEach { w ->
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(DateUtils.formatShortDate(w.monday), Modifier.width(LABEL_DP.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                w.days.forEach { d -> HeatCell(d, d.date == today, palette, if (compact) 13 else 18, Modifier.weight(1f)) { picked = d } }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.End)) {
            Text(picked?.let { "${DateUtils.formatShortDate(it.date)} · ${if (it.minutes > 0) DateUtils.formatMinutes(it.minutes) else "쉬는 날"}" } ?: "", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
            Text("적음", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            palette.heat.forEach { c -> Box(Modifier.size(width = 12.dp, height = 10.dp).background(c, RoundedCornerShape(3.dp))) }
            Text("많음", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HeatCell(day: HeatDay, isToday: Boolean, palette: ChartPalette, heightDp: Int, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(4.dp)
    val base = modifier.height(heightDp.dp)
    when {
        day.future -> Box(base.border(1.dp, palette.grid, shape))
        else -> Box(
            base.background(palette.heat[day.level], shape)
                .then(if (isToday) Modifier.border(2.dp, palette.ink, shape) else Modifier)
                .clickable(onClick = onClick),
        )
    }
}

private val DAYS = listOf("월", "화", "수", "목", "금", "토", "일")
private const val LABEL_DP = 34
