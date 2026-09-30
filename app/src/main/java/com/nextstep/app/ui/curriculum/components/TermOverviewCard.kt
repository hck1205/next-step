package com.nextstep.app.ui.curriculum.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.curriculum.CurriculumPlan
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.LabeledProgress

/** 이 학기에 길러야 할 것, 지금 시작할 것, 등록된 단원 비율. */
@Composable
internal fun TermOverviewCard(plan: CurriculumPlan) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("이 학기에 길러야 할 것", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            plan.curriculum.competencies.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
            plan.curriculum.startNow.forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
            LabeledProgress(label = "내 과목에 등록된 단원", ratio = plan.registeredRatio, color = MaterialTheme.colorScheme.primary)
            if (plan.essentialTodo.isNotEmpty()) Text("아직 등록 안 된 뼈대 단원 ${plan.essentialTodo.size}개 · 과목 카드의 '가져오기'로 한 번에", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}
