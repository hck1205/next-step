package com.nextstep.app.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.school.School
import com.nextstep.app.ui.components.layout.DetailSheet
import com.nextstep.app.ui.settings.SchoolSearchState

/**
 * 학교 고르기: 이름으로 찾아 고르면 이번 학년도 학사일정(방학 · 재량휴업일 · 시험 · 행사)이 가족 달력에 "학교" 일정으로 들어옵니다.
 * [current] 가 있으면 "지금 다시 받기"(새로 생긴 일정만 들어오고, 가족이 고치거나 지운 것은 그대로).
 */
@Composable
internal fun SchoolSheet(current: String, search: SchoolSearchState, onSearch: (String) -> Unit, onPick: (School) -> Unit, onSync: () -> Unit, onDismiss: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    DetailSheet("학교", onDismiss = onDismiss) {
        if (current.isNotBlank()) {
            Text("지금: $current", style = MaterialTheme.typography.bodyLarge)
            OutlinedButton(onClick = onSync, enabled = !search.busy) { Text("학사일정 지금 다시 받기") }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(query, { query = it }, label = { Text("학교 이름") }, singleLine = true, modifier = Modifier.weight(1f))
            Button(onClick = { onSearch(query) }, enabled = query.isNotBlank() && !search.busy) { Text("찾기") }
        }
        search.message?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
        search.results.forEach { school -> SchoolRow(school) { onPick(school) } }
    }
}

@Composable
private fun SchoolRow(school: School, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp)) {
        Text(school.name, style = MaterialTheme.typography.titleSmall)
        Text(listOf(school.kind, school.address).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
