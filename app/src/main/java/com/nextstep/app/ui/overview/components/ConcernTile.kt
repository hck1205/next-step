package com.nextstep.app.ui.overview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.hub.ConcernDigest
import com.nextstep.app.domain.hub.DigestChart
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.chart.MeterBar
import com.nextstep.app.ui.components.chart.MiniBars
import com.nextstep.app.ui.components.chart.Sparkline
import com.nextstep.app.ui.components.icon.concernIcon

/** 관심사 타일: 아이콘 · 이름 · 큰 한 줄 · 작은 한 줄 · 한 줄 차트(흐름·몫). 먼저 볼 곳이면 주황 점. 누르면 그 관심사 탭으로. */
@Composable
internal fun ConcernTile(digest: ConcernDigest, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier, onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(concernIcon(digest.concern), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text(digest.concern.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                if (digest.attention) Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape).semantics { contentDescription = "먼저 볼 곳" })
            }
            Text(digest.headline, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(digest.detail ?: " ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            digest.chart?.let { DigestChartRow(it, Modifier.padding(top = 4.dp)) }
        }
    }
}

/** 타일 아래 한 줄 차트. 값은 위 두 줄의 글이 이미 말하므로 차트는 흐름·몫만 보여 줍니다. */
@Composable
private fun DigestChartRow(chart: DigestChart, modifier: Modifier) {
    when (chart) {
        is DigestChart.Bars -> MiniBars(chart.values, modifier, height = CHART_DP.dp)
        is DigestChart.Line -> Sparkline(chart.values, modifier, height = CHART_DP.dp)
        is DigestChart.Meter -> MeterBar(chart.fraction, modifier)
    }
}

private const val CHART_DP = 24
