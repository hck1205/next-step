package com.nextstep.app.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.SubjectTag

/** 멘토가 맡은 과목. 고른 과목이 없으면 전 과목. */
@Composable
internal fun MySubjectsCard(subjects: List<SubjectEntity>, mine: List<String>) {
    AppCard {
        if (mine.isEmpty()) Text("전 과목 담당", style = MaterialTheme.typography.bodyMedium)
        else Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { subjects.filter { it.id in mine }.forEach { SubjectTag(it) } }
    }
}
