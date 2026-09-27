package com.nextstep.app.ui.yearplan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.year.YearTerm

/**
 * 학기 제목 줄(목록 위에 붙어 있음). 지금 학기는 강조하고, 접히는 학기([onFold] 있음)는 "n개 · 펼치기" / "접기"를 붙입니다.
 */
@Composable
internal fun TermHeader(term: YearTerm, isCurrent: Boolean, count: Int, expanded: Boolean, onFold: (() -> Unit)?) {
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
            .then(if (onFold != null) Modifier.clickable(onClick = onFold) else Modifier)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "${term.label} · ${term.months}" + if (isCurrent) " · 지금" else "",
            style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f),
            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
        if (onFold != null) Text(if (expanded) "접기" else "${count}개 · 펼치기", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    }
}
