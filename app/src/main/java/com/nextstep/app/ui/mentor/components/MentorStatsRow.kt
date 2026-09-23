package com.nextstep.app.ui.mentor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.oneDecimal
import com.nextstep.app.ui.components.card.StatTile

/** 이번 주 학습 · 내가 낸 과제 · 평균 점수 타일 세 개. */
@Composable
internal fun MentorStatsRow(weekMinutes: Int, openTaskCount: Int, averageScore: Double?) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile("이번 주 학습", DateUtils.formatMinutes(weekMinutes), Modifier.weight(1f), sub = "담당 과목 기준")
        StatTile("내가 낸 과제", "${openTaskCount}개", Modifier.weight(1f), tint = MaterialTheme.colorScheme.tertiary, sub = "미완료")
        StatTile("평균 점수", averageScore?.oneDecimal() ?: "-", Modifier.weight(1f), tint = MaterialTheme.colorScheme.secondary)
    }
}
