package com.nextstep.app.ui.components.row

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.reward.RewardStatus
import com.nextstep.app.domain.reward.RewardView
import com.nextstep.app.domain.time.DateUtils

/**
 * 보상 한 줄: 무엇을 · 언제(목표를 이루면 / 레벨에 닿으면) · 지금 어디까지.
 * [onGive]·[onCancel] 이 있으면(학부모·멘토) 받을 차례에 "줬어요", 약속에 "취소" 버튼이 붙습니다.
 */
@Composable
fun RewardRow(view: RewardView, onGive: (() -> Unit)? = null, onCancel: (() -> Unit)? = null, onOpen: (() -> Unit)? = null) {
    val status = view.status
    Row(
        (if (onOpen != null) Modifier.clickable(onClick = onOpen) else Modifier),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            Icons.Default.CardGiftcard, contentDescription = status.label, modifier = Modifier.size(22.dp),
            tint = when (status) {
                RewardStatus.EARNED -> MaterialTheme.colorScheme.primary
                RewardStatus.PROMISED -> MaterialTheme.colorScheme.secondary
                RewardStatus.GIVEN -> MaterialTheme.colorScheme.outline
            },
        )
        Column(Modifier.weight(1f)) {
            Text(view.reward.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                when (status) {
                    RewardStatus.GIVEN -> "받았어요 · ${view.target}" + (view.reward.givenAt?.let { " · ${DateUtils.formatShortDate(DateUtils.toLocalDate(it))}" } ?: "")
                    RewardStatus.EARNED -> "이뤘어요! · ${view.target}"
                    RewardStatus.PROMISED -> view.condition
                },
                style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                color = if (status == RewardStatus.EARNED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        when {
            status == RewardStatus.EARNED && onGive != null -> Button(onClick = onGive) { Text("줬어요") }
            status == RewardStatus.PROMISED && onCancel != null -> TextButton(onClick = onCancel) { Text("취소") }
            else -> Unit
        }
    }
}
