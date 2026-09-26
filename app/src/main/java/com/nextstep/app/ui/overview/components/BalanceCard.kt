package com.nextstep.app.ui.overview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.stats.BalanceReport
import com.nextstep.app.domain.stats.BalanceVerdict
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asPercent
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.LabeledProgress

/** 균형 카드: 한 줄 판단, 학습 게이지(단계 권장선 대비), 스스로 만든 계획 비율, 영유아기면 학원·수업 게이지, 연속 학습. 또래 비교는 없습니다. */
@Composable
internal fun BalanceCard(b: BalanceReport, yearLabel: String?) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(b.headline, style = MaterialTheme.typography.titleMedium)
            val ratio = if (b.recommendedWeekMinutes == 0) 0f else (b.weekMinutes.toFloat() / b.recommendedWeekMinutes).coerceIn(0f, 1f)
            LabeledProgress(
                label = "학습 · ${DateUtils.formatMinutes(b.weekMinutes)}",
                ratio = ratio,
                color = if (b.studyVerdict == BalanceVerdict.LESS) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trailing = b.studyVerdict.label,
            )
            Text(
                if (b.recommendedWeekMinutes == 0) b.studyLine else "${b.studyLine} · ${yearLabel ?: ""} 권장 주 ${DateUtils.formatMinutes(b.recommendedWeekMinutes)}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val self = b.selfDirectedRatio
            LabeledProgress(label = "스스로 만든 계획", ratio = self ?: 0f, color = MaterialTheme.colorScheme.tertiary, trailing = self?.asPercent() ?: "아직 없음")
            Text(
                when {
                    self == null -> "할 일이 쌓이면 학생이 스스로 만든 비율을 보여 드려요"
                    self >= SELF_DIRECTED_GOOD -> "스스로 계획하는 힘이 자라고 있어요"
                    else -> "어른이 만든 할 일이 더 많아요. 아이가 하나라도 직접 정하게 해 보세요"
                },
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (b.classCapWeekMinutes != null) {
                LabeledProgress(
                    label = "학원·수업 · ${DateUtils.formatMinutes(b.classWeekMinutes)}",
                    ratio = (b.classWeekMinutes.toFloat() / b.classCapWeekMinutes).coerceIn(0f, 1f),
                    color = if (b.classVerdict == BalanceVerdict.LESS) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    trailing = b.classVerdict.label,
                )
                b.classLine?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Text(
                if (b.streak == 0) "연속 학습 · 오늘 첫 기록을 남기면 시작돼요" else "연속 학습 · ${b.streak}일째",
                style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private const val SELF_DIRECTED_GOOD = 0.6f
