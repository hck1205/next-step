package com.nextstep.app.ui.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.input.DateField
import com.nextstep.app.ui.components.input.GradePicker
import java.time.LocalDate

/** 자녀 생년월일·학년. 생년월일이 없으면 왜 필요한지, 있으면 나이 표기를 보여 줍니다. */
@Composable
internal fun StudentProfileCard(birthDate: LocalDate?, ageLabel: String?, gradeYear: Int, onBirthDate: (LocalDate) -> Unit, onGradeYear: (Int) -> Unit) {
    AppCard {
        Column {
            DateField(label = "생년월일", date = birthDate ?: DateUtils.today().minusYears(DEFAULT_AGE_YEARS), onChange = onBirthDate)
            Text(
                if (birthDate == null) "생년월일을 넣으면 여정 타임라인(어린이집 대기, 검진, 입학, 입시 일정)이 자동으로 채워져요."
                else "$ageLabel · 학년은 생년월일로 자동 계산되며 아래에서 직접 바꿀 수 있어요.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            GradePicker(gradeYear = gradeYear, onSelect = onGradeYear)
            Text("학년은 추천 영상의 학년대, 학습 계획 길이, 학부모·멘토 가이드를 정합니다. 학생이나 학부모가 바꿀 수 있어요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** 생년월일을 아직 안 넣었을 때 날짜 선택기의 시작점. */
private const val DEFAULT_AGE_YEARS = 3L
