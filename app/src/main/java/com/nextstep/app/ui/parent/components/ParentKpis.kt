package com.nextstep.app.ui.parent.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nextstep.app.domain.stats.FamilyTrends
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.chart.MiniBars
import com.nextstep.app.ui.components.chart.Sparkline
import com.nextstep.app.ui.components.chart.StatGrid
import com.nextstep.app.ui.components.chart.StatTile
import com.nextstep.app.ui.components.chart.deltaText

/**
 * 상태 카드 아래 지표 네 칸: 최근 7일 공부(8주 흐름) · 이번 주 할 일(5주 달성) · 최근 점수(과목별 마지막 점수 평균) · 스스로 적은 할 일(몫).
 * 과목이 섞인 점수는 흐름선으로 잇지 않습니다(과목별 흐름은 시험 묶음의 과목별 점수 카드).
 */
@Composable
internal fun ParentKpis(t: FamilyTrends, modifier: Modifier = Modifier) {
    val thisWeek = t.weekRates.lastOrNull()
    val avg = t.scoreAverage
    val tiles = buildList<@Composable (Modifier) -> Unit> {
        add { m ->
            StatTile(
                "최근 7일 공부", DateUtils.formatMinutes(t.recent), m, delta = t.recentChange?.let { deltaText(it, DateUtils::formatMinutes, "전 7일 대비") },
                up = t.recentChange?.let { it >= 0 }, good = t.recentChange?.let { it >= 0 }, trend = { Sparkline(t.rolling) },
            )
        }
        add { m ->
            StatTile(
                "이번 주 할 일", thisWeek?.takeIf { it.total > 0 }?.let { "${it.percent}%" } ?: "-", m,
                delta = thisWeek?.let { "${it.done}/${it.total} 끝냄" }, trend = { MiniBars(t.weekRates.map { it.percent }) },
            )
        }
        if (avg != null) add { m ->
            StatTile(
                "최근 점수", "${avg}점", m, delta = t.scoreChange?.let { deltaText(it, { v -> "${v}점" }, "직전 대비") },
                up = t.scoreChange?.let { it >= 0 }, good = t.scoreChange?.let { it >= 0 },
            )
        }
        add { m -> StatTile("스스로 적은 할 일", "${(t.selfShare * PERCENT).toInt()}%", m, delta = "학생이 직접 만든 몫", meter = t.selfShare) }
    }
    StatGrid(tiles, modifier)
}

private const val PERCENT = 100
