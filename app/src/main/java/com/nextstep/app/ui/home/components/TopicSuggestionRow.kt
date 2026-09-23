package com.nextstep.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.SubjectTag

@Composable
internal fun TopicSuggestionRow(subject: SubjectEntity, topic: TopicEntity, actionLabel: String, onAction: () -> Unit, onAddTask: () -> Unit, onOpen: () -> Unit) {
    AppCard(onClick = onOpen) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SubjectTag(subject)
                Spacer(Modifier.width(8.dp))
                Text(topic.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onAddTask) { Text("할 일로 추가") }
                TextButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}
