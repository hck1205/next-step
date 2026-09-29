package com.nextstep.app.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.export.ExportDoc

/** 내보낼 문서(리포트·앨범)를 화면에 그대로: 제목 · 부제 · 덩어리마다 머리와 줄. */
@Composable
fun ExportDocCard(doc: ExportDoc) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(doc.title, style = MaterialTheme.typography.titleMedium)
            if (doc.subtitle.isNotBlank()) Text(doc.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            doc.sections.forEach { s ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(s.label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    s.lines.forEach { Text("· $it", style = MaterialTheme.typography.bodyMedium) }
                }
            }
        }
    }
}
