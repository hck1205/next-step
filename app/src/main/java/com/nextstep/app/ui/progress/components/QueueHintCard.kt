package com.nextstep.app.ui.progress.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.ui.components.card.AppCard

/** 이 과목에서 복습이 필요한 단원과 예습하면 좋은 단원(앞의 [previewHints]개) 한 줄씩. */
@Composable
internal fun QueueHintCard(reviewQueue: List<TopicEntity>, previewQueue: List<TopicEntity>, previewHints: Int) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (reviewQueue.isNotEmpty()) Text("복습 필요: ${reviewQueue.joinToString { it.title }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            if (previewQueue.isNotEmpty()) Text("예습 추천: ${previewQueue.take(previewHints).joinToString { it.title }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
