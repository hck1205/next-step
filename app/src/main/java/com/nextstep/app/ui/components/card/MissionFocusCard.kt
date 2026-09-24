package com.nextstep.app.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.mission.MissionFocus
import com.nextstep.app.ui.common.ratio

/**
 * 오늘 화면의 "다음 한 걸음": 날짜가 정해진 목표마다 D-day, 진행 막대, 다음 단계 한 줄.
 * 눌러서 목표 화면으로 갑니다. 목록은 3개까지(UX 가이드 1-5).
 */
@Composable
fun MissionFocusCard(items: List<MissionFocus>, onOpen: () -> Unit) {
    AppCard(onClick = onOpen) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.forEach { f ->
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(f.goal.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        DDayBadge(f.daysLeft)
                    }
                    Spacer(Modifier.height(4.dp))
                    LabeledProgress(label = "${f.doneCount}/${f.stepCount}", ratio = ratio(f.doneCount, f.stepCount), color = MaterialTheme.colorScheme.primary)
                    Text(
                        (if (f.overdueSteps > 0) "밀린 ${f.overdueSteps} · " else "") + "다음: ${f.nextStep.title}",
                        style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        color = if (f.overdueSteps > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
