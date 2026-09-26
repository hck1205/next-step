package com.nextstep.app.ui.goal.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.reward.RewardStatus
import com.nextstep.app.domain.reward.RewardView
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.row.RewardRow

/**
 * 이 목표에 걸린 보상(선택). 학부모·멘토([canGive])는 걸기 · 바꾸기 · 취소 · "줬어요", 학생은 보기만 합니다.
 * 보상이 없고 줄 수 없거나 이미 이룬 목표면 아무것도 그리지 않습니다.
 */
@Composable
internal fun GoalRewardCard(reward: RewardView?, canGive: Boolean, achieved: Boolean, onPromise: () -> Unit, onGive: () -> Unit, onCancel: () -> Unit) {
    if (reward == null) {
        if (!canGive || achieved) return
        AppCard {
            Column {
                Text("이루면 줄 보상을 걸 수 있어요(선택)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onPromise) { Text("보상 약속하기") }
            }
        }
        return
    }
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            RewardRow(reward, onGive = if (canGive) onGive else null, onCancel = if (canGive) onCancel else null)
            if (canGive && reward.status == RewardStatus.PROMISED) TextButton(onClick = onPromise) { Text("보상 바꾸기") }
        }
    }
}
