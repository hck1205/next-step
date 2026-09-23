package com.nextstep.app.ui.progress.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.common.ratio
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.LabeledProgress

/** 과목 상단 요약: 학급 진도·내 복습 막대, 진도 설정 칩, 역할별 안내 문장. */
@Composable
internal fun ProgressSummaryCard(topics: List<TopicEntity>, teacher: String?, color: Color, caps: Capabilities, onSetProgress: () -> Unit) {
    val total = topics.size
    val covered = topics.count { it.classCovered }
    val reviewed = topics.count { it.status.order >= TopicStatus.REVIEWED.order }
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LabeledProgress("학급 진도", ratio(covered, total), color.copy(alpha = 0.5f), trailing = "$covered/$total")
            LabeledProgress("내 복습", ratio(reviewed, total), color, trailing = "$reviewed/$total")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (caps.canEditTopics) AssistChip(onClick = onSetProgress, label = { Text("학급 진도 설정") })
                if (!teacher.isNullOrBlank()) Text("담당: $teacher", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterVertically))
            }
            Text(
                when {
                    caps.isStudent -> "체크 = 수업에서 배운 단원(학급 진도). 단원을 눌러 예습·복습 상태와 이해도를 기록하세요."
                    caps.canEditTopics -> "체크 = 수업에서 배운 단원. 단원을 등록하고 학급 진도를 갱신하면 학생에게 복습·예습 항목이 자동으로 뜹니다."
                    else -> "체크 = 수업에서 배운 단원. 상태와 이해도는 학생이 직접 기록한 값이에요."
                },
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
