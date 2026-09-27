package com.nextstep.app.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.stats.AssignerShare
import com.nextstep.app.ui.components.chart.BulletBars
import com.nextstep.app.ui.components.chart.BulletRow
import com.nextstep.app.ui.components.chart.ChartPalette
import com.nextstep.app.ui.components.chart.ShareBar
import com.nextstep.app.ui.components.chart.SharePart

/** 누가 준 할 일: 스스로·학부모·멘토의 몫(쌓은 막대). 자세히 보면 준 사람마다 끝낸 수(목표 대비 막대)까지. */
@Composable
fun AssignerCard(shares: List<AssignerShare>, compact: Boolean) {
    val palette = ChartPalette.current()
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ShareBar(shares.mapIndexed { i, s -> SharePart(labelOf(s.role), s.given, palette.series[i % palette.series.size]) })
            if (!compact) {
                Text("끝낸 몫", style = MaterialTheme.typography.labelLarge)
                BulletBars(
                    shares.map { s -> BulletRow(labelOf(s.role), s.done, s.given, "${s.done}개", "${s.given}개") },
                    doneLabel = "끝냄", goalLabel = "받은 것",
                )
            }
        }
    }
}

private fun labelOf(role: Role): String = if (role == Role.STUDENT) "스스로" else role.label
