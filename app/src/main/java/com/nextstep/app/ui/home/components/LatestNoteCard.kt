package com.nextstep.app.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard

/** 학부모·멘토가 남긴 가장 최근 한마디. */
@Composable
internal fun LatestNoteCard(note: NoteEntity) {
    AppCard {
        Column {
            Text("${note.authorName}의 한마디", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("“${note.text}”", style = MaterialTheme.typography.bodyLarge)
            Text(DateUtils.formatDate(DateUtils.toLocalDate(note.createdAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
