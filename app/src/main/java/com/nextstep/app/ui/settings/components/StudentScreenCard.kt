package com.nextstep.app.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.input.OptionPicker

/**
 * 학생 화면 단계 고르기. 기본은 학년에 맞춘 자동이고, 아이의 속도에 맞춰 한 단계 낮추거나 높일 수 있습니다.
 * 고른 값은 동기화되어 아이 기기의 화면이 바로 바뀝니다.
 */
@Composable
internal fun StudentScreenCard(auto: StudentUiLevel, chosen: StudentUiLevel?, onChoose: (StudentUiLevel?) -> Unit) {
    val options: List<StudentUiLevel?> = listOf<StudentUiLevel?>(null) + StudentUiLevel.entries
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OptionPicker(
                options = options, selected = chosen,
                label = { level -> if (level == null) "학년에 맞춰 자동 (지금 ${auto.label})" else "${level.label} · ${level.gradeSpan}" },
                onSelect = onChoose,
            )
            Text(
                "어릴수록 글씨가 크고 카드가 적어요. 학년이 오르면 새 카드가 하나씩 열려요.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
