package com.nextstep.app.ui.grades.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.LineChart
import com.nextstep.app.ui.components.LineSeries
import com.nextstep.app.ui.components.subjectColor
import com.nextstep.app.ui.grades.GradesUiState

/** 시험 날짜순으로 정렬해 과목별 계열을 그립니다. x축은 날짜별 고유 인덱스. */
@Composable
internal fun GradeTrendChart(state: GradesUiState) {
    val grades = state.filtered.sortedBy { it.date }
    val dates = grades.map { it.date }.distinct()
    val subjects = if (state.filterSubjectId == null) state.subjects.filter { s -> grades.any { it.subjectId == s.id } } else state.subjects.filter { it.id == state.filterSubjectId }
    val series = subjects.map { s ->
        LineSeries(
            name = s.name, color = subjectColor(s.color),
            points = dates.map { d -> grades.lastOrNull { it.subjectId == s.id && it.date == d }?.percent?.toFloat() },
        )
    }
    val classSeries = if (state.filterSubjectId != null) {
        val pts = dates.map { d -> grades.lastOrNull { it.date == d }?.classAverage?.toFloat() }
        if (pts.any { it != null }) LineSeries("반 평균", MaterialTheme.colorScheme.onSurfaceVariant, pts) else null
    } else null
    LineChart(
        series = series + listOfNotNull(classSeries),
        xLabels = dates.map { DateUtils.formatShortDate(DateUtils.fromEpochDay(it)) },
    )
}
