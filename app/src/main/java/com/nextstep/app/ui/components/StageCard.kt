package com.nextstep.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.growth.GrowthStage

/**
 * 성장 단계 카드. 정책이 없는 표시 전용 컴포넌트: 어떤 문장을 보여줄지는 호출하는 화면이 정합니다.
 * [stage] 가 null 이면 학년 입력을 유도하는 안내만 보여 줍니다.
 */
@Composable
fun StageCard(
    stage: GrowthStage?,
    gradeLabel: String?,
    headline: String?,
    body: String?,
    experience: String?,
    onSetGrade: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier) {
        Column {
            Row {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (stage == null) "성장 단계" else "성장 단계 · ${stage.label}${gradeLabel?.let { " ($it)" } ?: ""}",
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        if (stage == null) "학년을 입력하면 시기에 맞는 가이드를 보여 줘요" else stage.focus,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                if (stage == null && onSetGrade != null) TextButton(onClick = onSetGrade) { Text("학년 입력") }
            }
            if (headline != null) {
                Spacer(Modifier.height(6.dp))
                Text(headline, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            if (body != null) Text(body, style = MaterialTheme.typography.bodySmall)
            if (experience != null) {
                Spacer(Modifier.height(4.dp))
                Text("이번 주 경험 제안 · $experience", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}
