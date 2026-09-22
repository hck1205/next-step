package com.nextstep.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.growth.GrowthStage

/** 학년 선택 칩. 선택된 학년의 성장 단계 라벨을 함께 보여 줍니다. 0 이면 미선택. */
@Composable
fun GradePicker(gradeYear: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text("학년", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GrowthStage.gradeOptions().forEach { (year, label) ->
                FilterChip(selected = gradeYear == year, onClick = { onSelect(if (gradeYear == year) 0 else year) }, label = { Text(label) })
            }
        }
        GrowthStage.fromGradeYear(gradeYear)?.let { stage ->
            Text("${stage.label} · ${stage.focus}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
