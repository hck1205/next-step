package com.nextstep.app.ui.components.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nextstep.app.domain.stats.SubjectScore
import com.nextstep.app.ui.components.chart.RadarChart

/**
 * 과목 균형 레이더(평균 점수). [reviewRatios] 를 주면 같은 과목 순서의 복습 완료율을 겹쳐 그리고 색 설명을 붙입니다.
 * 성적 화면과 분석 화면이 같이 씁니다.
 */
@Composable
fun SubjectRadarCard(scores: List<SubjectScore>, reviewRatios: List<Float>? = null) {
    AppCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            RadarChart(
                axes = scores.map { it.subject.name },
                values = scores.map { (it.average / PERCENT).toFloat() },
                color = MaterialTheme.colorScheme.primary,
                secondary = reviewRatios,
                secondaryColor = MaterialTheme.colorScheme.secondary,
                chartSize = CHART_SIZE,
            )
            if (reviewRatios != null) Text("보라: 평균 점수 · 초록: 복습 완료율", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private const val PERCENT = 100.0
private const val CHART_SIZE = 240
