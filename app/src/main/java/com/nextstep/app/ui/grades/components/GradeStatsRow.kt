package com.nextstep.app.ui.grades.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.stats.SubjectScore
import com.nextstep.app.ui.common.oneDecimal
import com.nextstep.app.ui.components.card.StatTile

/** 전체 평균 · 가장 높은 과목 · 보완할 과목 세 칸. */
@Composable
internal fun GradeStatsRow(overallAverage: Double?, scores: List<SubjectScore>) {
    val best = scores.maxByOrNull { it.average }
    val weak = scores.minByOrNull { it.average }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile("전체 평균", overallAverage?.oneDecimal() ?: "-", Modifier.weight(1f))
        StatTile("최고 과목", best?.subject?.name ?: "-", Modifier.weight(1f), tint = MaterialTheme.colorScheme.secondary, sub = best?.let { "${it.average.oneDecimal()}점" })
        StatTile("보완 과목", weak?.subject?.name ?: "-", Modifier.weight(1f), tint = MaterialTheme.colorScheme.tertiary, sub = weak?.let { "${it.average.oneDecimal()}점" })
    }
}
