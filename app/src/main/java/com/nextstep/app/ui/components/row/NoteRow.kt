package com.nextstep.app.ui.components.row

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard

/** 메모·격려·피드백 한 줄. 작성자와 날짜, 내가 쓴 것이면 삭제 버튼. */
@Composable
fun NoteRow(note: NoteEntity, onDelete: (() -> Unit)?, showTime: Boolean = false) {
    val stamp = DateUtils.formatDate(DateUtils.toLocalDate(note.createdAt)) + if (showTime) " " + DateUtils.formatTime(note.createdAt) else ""
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(note.text, style = MaterialTheme.typography.bodyLarge)
                Text("${note.authorName} (${Role.labelOf(note.authorRole)}) · $stamp", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (onDelete != null) TextButton(onClick = onDelete) { Text("삭제") }
        }
    }
}
