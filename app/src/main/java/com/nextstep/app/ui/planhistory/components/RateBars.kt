package com.nextstep.app.ui.planhistory.components

import androidx.compose.runtime.Composable
import com.nextstep.app.domain.goaltree.RateBy
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.chart.BulletBars
import com.nextstep.app.ui.components.chart.BulletRow

/** 묶음(과목 등)별 달성: 막대 = 끝낸 할 일, 세로 선 = 마감이던 할 일. */
@Composable
internal fun RateBars(rows: List<RateBy>) {
    AppCard {
        BulletBars(rows.map { r -> BulletRow(r.label, r.done, r.total, "${r.done}개", "${r.total}개") }, doneLabel = "끝냄", goalLabel = "마감이던 것")
    }
}
