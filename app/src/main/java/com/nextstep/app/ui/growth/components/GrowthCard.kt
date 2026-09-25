package com.nextstep.app.ui.growth.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.health.GrowthSignalLevel
import com.nextstep.app.domain.health.GrowthSummary
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.common.oneDecimal

/** 신체 섹션 맨 위 요약: 최근 키·몸무게·시력, 속도, 확인할 신호. 기록 목록은 [GrowthRecordRow] 가 연도별로 그립니다. */
@Composable
internal fun GrowthCard(summary: GrowthSummary?, onAdd: (() -> Unit)?, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("성장 기록 · 키 · 몸무게 · 시력", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (summary == null || !summary.hasAny) "검진 결과나 집에서 잰 값을 남겨 두면 변화가 보여요"
                        else listOfNotNull(summary.heightCm?.let { "키 ${growthNumber(it)}cm" }, summary.weightKg?.let { "몸무게 ${growthNumber(it)}kg" }, visionText(summary.visionLeft, summary.visionRight)).joinToString(" · "),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                if (onAdd != null) TextButton(onClick = onAdd) { Text("기록") }
            }
            if (summary != null && summary.hasAny) {
                Text(
                    listOfNotNull(
                        summary.latestDate?.let { "최근 ${DateUtils.formatDate(it)}" },
                        summary.heightVelocityCmPerYear?.let { "키 연 ${growthNumber(it)}cm 속도" },
                        summary.weightDeltaKg?.let { "몸무게 ${if (it >= 0) "+" else ""}${growthNumber(it)}kg" },
                        summary.bmi?.let { "BMI ${growthNumber(it)}" },
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                summary.signals.forEach { s ->
                    Spacer(Modifier.height(2.dp))
                    Text(s.title, style = MaterialTheme.typography.labelLarge, color = if (s.level == GrowthSignalLevel.CHECK) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    Text(s.detail, style = MaterialTheme.typography.bodySmall)
                }
                Text("비교 대상은 또래가 아니라 지난 기록이에요. 백분위는 검진에서 의사와 함께 보세요.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

internal fun visionText(left: Double?, right: Double?): String? = if (left == null && right == null) null else "시력 ${left?.let { growthNumber(it) } ?: "-"} / ${right?.let { growthNumber(it) } ?: "-"}"

internal fun growthNumber(v: Double): String = v.oneDecimal()
