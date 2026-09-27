package com.nextstep.app.ui.mentor.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nextstep.app.domain.stats.FamilyTrends
import com.nextstep.app.domain.stats.SubjectProgress
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.chart.Sparkline
import com.nextstep.app.ui.components.chart.StatGrid
import com.nextstep.app.ui.components.chart.StatTile
import com.nextstep.app.ui.components.chart.deltaText

/**
 * 멘토 오늘 화면 맨 위 지표 네 칸(담당 과목 기준): 낸 과제(끝낸 몫) · 최근 7일 공부(8주 흐름) · 최근 점수 · 복습 밀린 단원(복습한 몫).
 * 점수 흐름선은 담당 과목이 하나일 때만(과목이 섞인 점수는 잇지 않음).
 */
@Composable
internal fun MentorKpis(t: FamilyTrends, progress: List<SubjectProgress>, modifier: Modifier = Modifier) {
    val sub = t.submissions
    val covered = progress.sumOf { it.classCovered }
    val reviewed = progress.sumOf { it.reviewed }
    val avg = t.scoreAverage
    val tiles = buildList<@Composable (Modifier) -> Unit> {
        add { m ->
            StatTile(
                "낸 과제", "${sub.done}/${sub.total}", m, delta = if (sub.late > 0) "밀린 과제 ${sub.late}개" else "밀린 과제 없음",
                good = sub.late == 0, meter = if (sub.total > 0) sub.done.toFloat() / sub.total else 0f,
            )
        }
        add { m ->
            StatTile(
                "최근 7일 공부", DateUtils.formatMinutes(t.recent), m, delta = t.recentChange?.let { deltaText(it, DateUtils::formatMinutes, "전 7일 대비") },
                up = t.recentChange?.let { it >= 0 }, good = t.recentChange?.let { it >= 0 }, trend = { Sparkline(t.rolling) },
            )
        }
        if (avg != null) add { m ->
            StatTile(
                "최근 점수", "${avg}점", m, delta = t.scoreChange?.let { deltaText(it, { v -> "${v}점" }, "직전 대비") },
                up = t.scoreChange?.let { it >= 0 }, good = t.scoreChange?.let { it >= 0 },
                trend = { t.scores.singleOrNull()?.let { Sparkline(it.percents) } },
            )
        }
        if (progress.isNotEmpty()) add { m ->
            StatTile(
                "복습 밀린 단원", "${(covered - reviewed).coerceAtLeast(0)}개", m, delta = "수업 $covered · 복습 $reviewed",
                good = covered - reviewed <= 1, meter = if (covered > 0) reviewed.toFloat() / covered else 0f,
            )
        }
    }
    StatGrid(tiles, modifier)
}
