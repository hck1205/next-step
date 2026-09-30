package com.nextstep.app.ui.roadmap.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity

/** 진도 기반 추천을 칩으로. 누르면 로드맵에 바로 추가됩니다. */
@Composable
internal fun SuggestionChips(suggestions: List<Pair<SubjectEntity, String>>, onAdd: (SubjectEntity, String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        suggestions.forEach { (subject, title) ->
            AssistChip(
                onClick = { onAdd(subject, title) },
                label = { Text("${subject.name} · $title") },
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
            )
        }
    }
}
