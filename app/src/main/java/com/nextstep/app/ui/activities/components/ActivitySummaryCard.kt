package com.nextstep.app.ui.activities.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nextstep.app.data.model.ActivityType
import com.nextstep.app.ui.components.card.AppCard

/** 활동 수 요약(전체 · 이번 구간 · 진행 중)과 종류별 개수. 아직 없으면 첫 기록을 권합니다. */
@Composable
internal fun ActivitySummaryCard(total: Int, currentPeriodCount: Int, ongoingCount: Int, countByType: Map<ActivityType, Int>) {
    AppCard {
        Column {
            Text(
                if (total == 0) "첫 활동을 기록해 보세요. 현장학습 한 번, 취미 시작도 좋아요."
                else "총 ${total}개 · 이번 구간 ${currentPeriodCount}개 · 진행 중인 취미·동아리 ${ongoingCount}개",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (countByType.isNotEmpty()) Text(
                countByType.entries.joinToString(" · ") { "${it.key.label} ${it.value}" },
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
