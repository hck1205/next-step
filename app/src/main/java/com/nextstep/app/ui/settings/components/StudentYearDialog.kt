package com.nextstep.app.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.ui.components.input.OptionPicker
import java.time.LocalDate

/**
 * 생년월일·학년·아이 화면 단계를 고치는 창. 고른 값은 "바꾸기"를 눌러야 저장되어,
 * 1년 동안 쓰는 값을 실수로 바꾸지 않게 합니다. 아이 화면 단계는 [canChooseLevel] 일 때만 보입니다.
 */
@Composable
internal fun StudentYearDialog(
    birthDate: LocalDate?,
    ageLabel: String?,
    gradeYear: Int,
    auto: StudentUiLevel?,
    chosen: StudentUiLevel?,
    canChooseLevel: Boolean,
    onSave: (birthDate: LocalDate?, gradeYear: Int, level: StudentUiLevel?) -> Unit,
    onDismiss: () -> Unit,
) {
    var birth by remember { mutableStateOf(birthDate) }
    var grade by remember { mutableStateOf(gradeYear) }
    var level by remember { mutableStateOf(chosen) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("학년 고치기") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "학년은 1년 동안 오늘의 카드·올해 할 일·권장 공부량을 정해요. 생년월일이 맞으면 보통 고칠 필요가 없어요.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StudentProfileCard(birthDate = birth, ageLabel = ageLabel, gradeYear = grade, onBirthDate = { birth = it }, onGradeYear = { grade = it })
                if (canChooseLevel && auto != null) StudentScreenCard(auto = auto, chosen = level, onChoose = { level = it })
            }
        },
        confirmButton = { TextButton(onClick = { onSave(birth, grade, level) }) { Text("바꾸기") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
