package com.nextstep.app.ui.curriculum.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.curriculum.CurriculumUnit
import com.nextstep.app.domain.curriculum.SubjectPlan
import com.nextstep.app.ui.components.card.AppCard

/** 과목 하나의 커리큘럼 단원 목록. 내 과목이 있으면 카드로 열고, 미등록 단원은 가져올 수 있습니다. */
@Composable
internal fun SubjectPlanCard(
    sp: SubjectPlan,
    caps: Capabilities,
    onOpenSubject: (String) -> Unit,
    onImport: () -> Unit,
    onAddTask: (CurriculumUnit) -> Unit,
    onOpenUrl: (String) -> Unit,
    onWatched: (String) -> Unit,
) {
    AppCard(onClick = sp.familySubject?.let { fs -> { onOpenSubject(fs.id) } }) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(sp.subject, style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (sp.familySubject == null) "내 과목에 없음 · 단원 ${sp.units.size}개" else "내 과목 '${sp.familySubject.name}' · 미등록 ${sp.notRegistered.size}개",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (caps.canEditTopics && sp.notRegistered.isNotEmpty()) TextButton(onClick = onImport) { Text("가져오기") }
            }
            sp.units.forEach { up ->
                UnitRow(plan = up, onAddTask = if (caps.canCreateTasks) ({ onAddTask(up.unit) }) else null, onOpenUrl = onOpenUrl, onWatched = onWatched)
            }
        }
    }
}
