package com.nextstep.app.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.goaltree.Assigner
import com.nextstep.app.domain.goaltree.RateBy
import com.nextstep.app.ui.components.chart.BulletBars
import com.nextstep.app.ui.components.chart.BulletRow
import com.nextstep.app.ui.components.chart.ChartPalette
import com.nextstep.app.ui.components.chart.ShareBar
import com.nextstep.app.ui.components.chart.SharePart

/**
 * 누가 준 할 일(최근 4주): 스스로·학부모가·멘토가의 몫(쌓은 막대). 자세히 보면 준 사람마다 끝낸 수(목표 대비 막대)까지.
 * 색은 준 사람을 따라갑니다([Assigner] 순서) — 한 사람이 빠져도 나머지 색은 그대로.
 */
@Composable
fun AssignerCard(byAssigner: List<RateBy>, compact: Boolean) {
    val palette = ChartPalette.current()
    val colorOf = { r: RateBy -> palette.series[(Assigner.of(r.key)?.ordinal ?: 0) % palette.series.size] }
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ShareBar(byAssigner.map { SharePart(it.label, it.total, colorOf(it)) })
            if (!compact) {
                Text("끝낸 몫", style = MaterialTheme.typography.labelLarge)
                BulletBars(byAssigner.map { BulletRow(it.label, it.done, it.total, "${it.done}개", "${it.total}개") }, doneLabel = "끝냄", goalLabel = "받은 것")
            }
        }
    }
}
