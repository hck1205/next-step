package com.nextstep.app.ui.home.components

import androidx.compose.material3.IconButton
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.SubjectTag

/**
 * 어린 단계의 할 일 한 줄: 큰 동그라미 체크 + 과목 배지 + 제목뿐. 날짜·종류·배정자 같은 보조 글자는 없습니다.
 * 줄 전체가 누름 영역이고 높이는 화면 단계의 [minHeightDp] 이상입니다. [onSpeak] 가 있으면 스피커로 제목을 읽어 줍니다.
 */
@Composable
internal fun BigTaskRow(task: TaskEntity, subjects: List<SubjectEntity>, minHeightDp: Int, onToggle: () -> Unit, onSpeak: ((String) -> Unit)? = null) {
    val subject = subjects.firstOrNull { it.id == task.subjectId }
    AppCard(
        modifier = Modifier.heightIn(min = minHeightDp.dp).semantics {
            role = Role.Checkbox
            stateDescription = if (task.done) "했어요" else "아직"
        },
        onClick = onToggle,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            val circle = Modifier.size((minHeightDp - CIRCLE_INSET_DP).dp)
            Box(
                if (task.done) circle.background(MaterialTheme.colorScheme.secondary, CircleShape)
                else circle.border(3.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (task.done) Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                if (subject != null) SubjectTag(subject)
            }
            if (onSpeak != null) IconButton(onClick = { onSpeak(task.title) }) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "읽어 주기", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/** 동그라미가 줄 높이보다 이만큼 작습니다. */
private const val CIRCLE_INSET_DP = 20
