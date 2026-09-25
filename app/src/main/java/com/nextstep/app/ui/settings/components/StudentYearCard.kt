package com.nextstep.app.ui.settings.components

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
import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import java.time.LocalDate

/**
 * 자녀의 올해 학년 요약 한 줄. 학년은 한 번 정하면 1년을 가므로 고르는 칩을 늘어놓지 않고,
 * "고치기"를 눌러야 바꾸는 창이 열립니다([StudentYearDialog]).
 */
@Composable
internal fun StudentYearCard(yearLabel: String?, birthDate: LocalDate?, ageLabel: String?, level: StudentUiLevel?, chosen: Boolean, onEdit: () -> Unit) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    listOfNotNull(yearLabel ?: "학년 미정", birthDate?.let { DateUtils.formatFullDate(it) + "생" }, ageLabel).joinToString(" · "),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    if (birthDate == null) "생년월일을 넣으면 학년과 올해 할 일이 자동으로 채워져요."
                    else "해마다 3월에 한 학년씩 자동으로 올라가요.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                level?.let {
                    Text(
                        "아이 화면: ${it.label}" + if (chosen) " (직접 고름)" else " (학년에 맞춤)",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            TextButton(onClick = onEdit) { Text(if (birthDate == null) "넣기" else "고치기") }
        }
    }
}
