package com.nextstep.app.ui.components.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.stats.HeatWeek

/**
 * 한 해 공부 달력(가로): 열 = 주, 줄 = 월~일, 진하기 = 공부한 양(ChartPalette.heat). 달이 바뀌는 주 위에 "3월" 같은 표시.
 * 주가 많으면 옆으로 밀고, 처음에는 가장 최근 주가 보이게 오른쪽 끝에서 시작합니다.
 */
@Composable
fun YearHeatStrip(weeks: List<HeatWeek>, modifier: Modifier = Modifier) {
    val palette = ChartPalette.current()
    val scroll = rememberScrollState()
    LaunchedEffect(weeks.size, scroll.maxValue) { scroll.scrollTo(scroll.maxValue) }
    val active = weeks.sumOf { it.activeDays }
    Column(modifier.fillMaxWidth().semantics { contentDescription = "${weeks.size}주 공부 달력: ${active}일 공부" }, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.horizontalScroll(scroll), horizontalArrangement = Arrangement.spacedBy(GAP.dp)) {
            weeks.forEachIndexed { i, w ->
                val newMonth = i == 0 || w.monday.monthValue != weeks[i - 1].monday.monthValue
                Column(verticalArrangement = Arrangement.spacedBy(GAP.dp)) {
                    Box(Modifier.height(16.dp)) {
                        if (newMonth) Text("${w.monday.monthValue}월", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, softWrap = false)
                    }
                    w.days.forEach { d ->
                        val shape = RoundedCornerShape(3.dp)
                        Box(if (d.future) Modifier.size(CELL.dp).border(1.dp, palette.grid, shape) else Modifier.size(CELL.dp).background(palette.heat[d.level], shape))
                    }
                }
            }
        }
        HeatLegend(Modifier.align(Alignment.End))
    }
}

private const val CELL = 14
private const val GAP = 4
