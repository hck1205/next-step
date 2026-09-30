package com.nextstep.app.ui.quickadd.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.growth.KidRecord

/**
 * 아이용 기록하기: 큰 그림 타일 2열. 한 번 누르면 끝(양식 없음). [onTimer] 가 있으면 맨 앞에 "공부 시작" 타일.
 */
@Composable
internal fun KidRecordGrid(onRecord: (KidRecord) -> Unit, onTimer: (() -> Unit)?) {
    val tiles: List<Pair<String, Pair<ImageVector, () -> Unit>>> =
        listOfNotNull(onTimer?.let { "공부 시작" to (Icons.Default.Timer to it) }) +
            KidRecord.entries.map { r -> r.label to (iconFor(r) to { onRecord(r) }) }
    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("오늘 뭐 했어요?", style = MaterialTheme.typography.titleLarge)
        tiles.chunked(COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (label, pair) ->
                    val (icon, onClick) = pair
                    Column(
                        Modifier.weight(1f).heightIn(min = TILE_HEIGHT.dp).clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer).clickable(onClick = onClick)
                            .semantics { role = Role.Button }.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                    ) {
                        Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(40.dp)) }
                        Spacer(Modifier.size(8.dp))
                        Text(label, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
                if (row.size < COLUMNS) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private fun iconFor(record: KidRecord): ImageVector = when (record) {
    KidRecord.READ -> Icons.AutoMirrored.Filled.MenuBook
    KidRecord.EXERCISE -> Icons.AutoMirrored.Filled.DirectionsRun
    KidRecord.DRAW -> Icons.Default.Brush
    KidRecord.MUSIC -> Icons.Default.MusicNote
    KidRecord.HELP -> Icons.Default.CleaningServices
    KidRecord.OUTING -> Icons.Default.Park
}

private const val COLUMNS = 2
private const val TILE_HEIGHT = 110
