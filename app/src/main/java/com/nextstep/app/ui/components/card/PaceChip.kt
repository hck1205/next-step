package com.nextstep.app.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.project.ProjectPace

/** 교육 프로젝트의 계획 대비 속도 칩. 늦어도 빨간색 대신 차분한 색으로(비교 대상은 계획뿐). */
@Composable
fun PaceChip(pace: ProjectPace, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val (bg, fg) = when (pace) {
        ProjectPace.AHEAD, ProjectPace.DONE -> scheme.primaryContainer to scheme.onPrimaryContainer
        ProjectPace.ON_TRACK -> scheme.secondaryContainer to scheme.onSecondaryContainer
        ProjectPace.BEHIND -> scheme.tertiaryContainer to scheme.onTertiaryContainer
    }
    Text(
        pace.label, style = MaterialTheme.typography.labelSmall, color = fg,
        modifier = modifier.clip(RoundedCornerShape(8.dp)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp),
    )
}
