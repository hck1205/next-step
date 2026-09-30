package com.nextstep.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.SubjectTag

/** 멘토 로드맵 항목 한 줄. 계획 상태면 "시작", 진행 중이면 "완료" 버튼. */
@Composable
internal fun RoadmapFocusRow(item: RoadmapItemEntity, subject: SubjectEntity?, onOpen: () -> Unit, onStatus: (RoadmapStatus) -> Unit) {
    AppCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (subject != null) SubjectTag(subject)
                    Text(item.status.label + (item.targetDate?.let { " · ${DateUtils.dDay(DateUtils.fromEpochDay(it))}" } ?: ""), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    if (item.createdByName.isNotBlank()) Text("${item.createdByName} 제안", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (item.status == RoadmapStatus.PLANNED) TextButton(onClick = { onStatus(RoadmapStatus.IN_PROGRESS) }) { Text("시작") }
            else TextButton(onClick = { onStatus(RoadmapStatus.DONE) }) { Text("완료") }
        }
    }
}
