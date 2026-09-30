package com.nextstep.app.ui.components.input

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nextstep.app.domain.growth.GrowthStage

/** 학년 선택 칩. 선택된 학년의 성장 단계 라벨을 함께 보여 줍니다. 0 이면 미선택. */
@Composable
fun GradePicker(gradeYear: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        ChipRow(GrowthStage.gradeOptions(), { it.first == gradeYear }, { it.second }, { (year, _) -> onSelect(if (gradeYear == year) 0 else year) }, title = "학년")
        GrowthStage.fromGradeYear(gradeYear)?.let { stage ->
            Text("${stage.label} · ${stage.focus}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
