package com.nextstep.app.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.gamify.GameProfile
import com.nextstep.app.domain.reward.RewardStatus
import com.nextstep.app.domain.reward.RewardView

/**
 * 나의 레벨(학생 오늘 · 기록 › 보상·배지): 레벨과 경험치 막대, 연속 기록, 이번 주 도전 3가지, 받은 배지, 다음 보상 한 줄.
 * [showsNumbers] 가 false 인 어린 화면은 경험치 숫자 대신 막대와 체크만 보여 줍니다.
 * 점수를 깎거나 남과 비교하는 표시는 두지 않습니다.
 */
@Composable
fun GameCard(profile: GameProfile, reward: RewardView?, showsNumbers: Boolean = true, onOpen: (() -> Unit)? = null) {
    val level = profile.level
    AppCard(onClick = onOpen) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Column(Modifier.weight(1f)) {
                    Text("레벨 ${level.number} · ${level.title}", style = MaterialTheme.typography.titleMedium)
                    if (showsNumbers) Text("${profile.xp} XP · 다음 레벨까지 ${level.remaining(profile.xp)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (profile.stats.streak > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = "연속", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                        Text("${profile.stats.streak}일", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
            val bar = MaterialTheme.colorScheme.primary
            LinearProgressIndicator(progress = { level.progress(profile.xp) }, modifier = Modifier.fillMaxWidth().height(10.dp), color = bar, trackColor = bar.copy(alpha = TRACK_ALPHA))
            if (profile.challenges.isNotEmpty()) {
                Text("이번 주 도전 ${profile.challengesDone}/${profile.challenges.size}", style = MaterialTheme.typography.labelLarge)
                profile.challenges.forEach { c ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            if (c.complete) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, contentDescription = if (c.complete) "해냈어요" else "아직",
                            tint = if (c.complete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp),
                        )
                        Text(c.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        if (showsNumbers) Text("${c.done.coerceAtMost(c.target)}/${c.target}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            val earned = profile.earnedBadges
            if (earned.isNotEmpty() || profile.nextBadges.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Text(
                        listOfNotNull(
                            earned.takeLast(RECENT_BADGES).joinToString(" · ") { it.badge.label }.takeIf { it.isNotBlank() },
                            profile.nextBadges.firstOrNull()?.let { "다음: ${it.badge.label}" },
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            reward?.let { r ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = "보상", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text(
                        if (r.status == RewardStatus.EARNED) "받을 차례 · ${r.reward.title}" else "${r.condition} · ${r.reward.title}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private const val RECENT_BADGES = 2
private const val TRACK_ALPHA = 0.15f
