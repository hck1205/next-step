package com.nextstep.app.ui.records.components

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
import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.domain.health.GrowthSignalLevel
import com.nextstep.app.domain.health.GrowthSummary
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.AppCard
import java.time.LocalDate
import java.util.Locale

/** 성장 기록 카드: 최신 키·몸무게·시력, 지난 기록 대비 변화, 참고 신호, 최근 기록 목록. 정책 없이 콜백만 올립니다. */
@Composable
fun GrowthCard(summary: GrowthSummary?, records: List<GrowthRecordEntity>, onAdd: (() -> Unit)?, onEdit: (GrowthRecordEntity) -> Unit, onDelete: ((String) -> Unit)?, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("성장 기록 · 키 · 몸무게 · 시력", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (summary == null || !summary.hasAny) "검진 결과나 집에서 잰 값을 남겨 두면 변화가 보여요"
                        else listOfNotNull(summary.heightCm?.let { "키 ${num(it)}cm" }, summary.weightKg?.let { "몸무게 ${num(it)}kg" }, vision(summary.visionLeft, summary.visionRight)).joinToString(" · "),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                if (onAdd != null) TextButton(onClick = onAdd) { Text("기록") }
            }
            if (summary != null && summary.hasAny) {
                Text(
                    listOfNotNull(
                        summary.latestDate?.let { "최근 ${DateUtils.formatDate(it)}" },
                        summary.heightVelocityCmPerYear?.let { "키 연 ${num(it)}cm 속도" },
                        summary.weightDeltaKg?.let { "몸무게 ${if (it >= 0) "+" else ""}${num(it)}kg" },
                        summary.bmi?.let { "BMI ${num(it)}" },
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                summary.signals.forEach { s ->
                    Spacer(Modifier.height(2.dp))
                    Text(s.title, style = MaterialTheme.typography.labelLarge, color = if (s.level == GrowthSignalLevel.CHECK) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    Text(s.detail, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(4.dp))
                records.forEach { r ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${DateUtils.formatDate(LocalDate.ofEpochDay(r.date))} · " + listOfNotNull(r.heightCm?.let { "${num(it)}cm" }, r.weightKg?.let { "${num(it)}kg" }, vision(r.visionLeft, r.visionRight)).joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { onEdit(r) }) { Text("수정") }
                        if (onDelete != null) TextButton(onClick = { onDelete(r.id) }) { Text("삭제", color = MaterialTheme.colorScheme.error) }
                    }
                }
                Text("비교 대상은 또래가 아니라 지난 기록이에요. 백분위는 검진에서 의사와 함께 보세요.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun vision(left: Double?, right: Double?): String? = if (left == null && right == null) null else "시력 ${left?.let { num(it) } ?: "-"} / ${right?.let { num(it) } ?: "-"}"

private fun num(v: Double): String = if (v == v.toLong().toDouble()) v.toLong().toString() else String.format(Locale.ROOT, "%.1f", v)
