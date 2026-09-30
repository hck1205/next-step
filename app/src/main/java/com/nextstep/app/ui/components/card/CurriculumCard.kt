package com.nextstep.app.ui.components.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nextstep.app.domain.curriculum.TermCurriculum

/** 이번 학기 교과 요약 카드: 과목 수, 뼈대 단원 몇 개, 첫 역량 한 줄. 눌러 커리큘럼 화면으로. */
@Composable
fun CurriculumCard(curriculum: TermCurriculum, periodLabel: String, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val essential = curriculum.units.filter { it.essential }
    AppCard(modifier = modifier, onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("$periodLabel 교과 커리큘럼", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${curriculum.subjects.joinToString(" · ")} · 뼈대 단원 ${essential.size}개", style = MaterialTheme.typography.titleSmall)
                curriculum.competencies.firstOrNull()?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                essential.take(2).forEach { Text("• ${it.subject} ${it.title}", style = MaterialTheme.typography.bodySmall) }
            }
            TextButton(onClick = onOpen) { Text("보기") }
        }
    }
}
