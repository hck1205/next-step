package com.nextstep.app.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import com.nextstep.app.domain.gamify.GameStyle
import com.nextstep.app.domain.gamify.WeekChallenge
import com.nextstep.app.domain.reward.RewardStatus
import com.nextstep.app.domain.reward.RewardView

/**
 * 나의 스티커판 · 나의 레벨 · 나의 성장 기록(학생 오늘 · 기록 › 보상·배지). 모양은 나이에 맞춘 [GameProfile.style] 이 정합니다.
 * - 스티커판: 이번 주 10칸(한 일마다 한 장) · 채운 판 수. 레벨·경험치·연속 기록 없음.
 * - 레벨: 레벨 이름과 경험치 막대 · 연속 일수 · 이번 주 도전 셋.
 * - 성장 기록: Lv 과 누적 기록(공부 시간 · 스스로 한 일 · 계획한 주) · 쉬어도 이어지는 연속 · 내 계획 지키기.
 * 모든 모양에 받은 배지와 다음 보상 한 줄. [showsNumbers] 가 false 인 어린 화면은 숫자를 줄입니다. 점수를 깎거나 비교하는 표시는 없습니다.
 */
@Composable
fun GameCard(profile: GameProfile, reward: RewardView?, showsNumbers: Boolean = true, onOpen: (() -> Unit)? = null) {
    AppCard(onClick = onOpen) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (profile.style) {
                GameStyle.STICKERS -> StickerBoard(profile)
                GameStyle.LEVELS -> LevelHeader(profile, showsNumbers)
                GameStyle.GROWTH -> GrowthHeader(profile, showsNumbers)
            }
            if (profile.style != GameStyle.STICKERS) ChallengeRows(profile.challenges, profile.challengesDone, showsNumbers)
            BadgeLine(profile)
            reward?.let { RewardLine(it) }
        }
    }
}

@Composable
private fun StickerBoard(profile: GameProfile) {
    val filled = profile.stickersThisWeek.coerceAtMost(GameStyle.BOARD_SIZE)
    Text(profile.style.title, style = MaterialTheme.typography.titleMedium)
    (0 until GameStyle.BOARD_SIZE).chunked(STICKERS_PER_ROW).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { i ->
                val on = i < filled
                Icon(
                    if (on) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = if (on) "스티커" else "빈칸",
                    tint = if (on) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline, modifier = Modifier.size(STICKER_DP.dp),
                )
            }
        }
    }
    Text(
        if (filled >= GameStyle.BOARD_SIZE) "이번 주 스티커판을 다 채웠어요!" else "한 일마다 스티커 한 장",
        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary,
    )
    if (profile.boards > 0) Text("지금까지 채운 스티커판 ${profile.boards}장", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun LevelHeader(profile: GameProfile, showsNumbers: Boolean) {
    val level = profile.level
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
        Column(Modifier.weight(1f)) {
            Text("레벨 ${level.number} · ${level.title}", style = MaterialTheme.typography.titleMedium)
            if (showsNumbers) Text("${profile.xp} XP · 다음 레벨까지 ${level.remaining(profile.xp)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Streak(profile.stats.streak)
    }
    LevelBar(profile)
}

@Composable
private fun GrowthHeader(profile: GameProfile, showsNumbers: Boolean) {
    val level = profile.level
    val s = profile.stats
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Column(Modifier.weight(1f)) {
            Text("${profile.style.title} · Lv ${level.number}", style = MaterialTheme.typography.titleMedium)
            if (showsNumbers) Text("${profile.xp} XP · 다음까지 ${level.remaining(profile.xp)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Streak(s.streak)
    }
    LevelBar(profile)
    Text(
        "지금까지 공부 ${s.studyMinutes / MINUTES_PER_HOUR}시간 · 스스로 한 일 ${s.selfDone}개 · 계획한 주 ${s.weekPlans}",
        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (profile.style.restDays > 0) Text("연속 기록은 하루 쉬어도 이어져요", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun Streak(days: Int) {
    if (days <= 0) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.LocalFireDepartment, contentDescription = "연속", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
        Text("${days}일", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun LevelBar(profile: GameProfile) {
    val bar = MaterialTheme.colorScheme.primary
    LinearProgressIndicator(
        progress = { profile.level.progress(profile.xp) }, modifier = Modifier.fillMaxWidth().height(10.dp), color = bar, trackColor = bar.copy(alpha = TRACK_ALPHA),
    )
}

@Composable
private fun ChallengeRows(challenges: List<WeekChallenge>, done: Int, showsNumbers: Boolean) {
    if (challenges.isEmpty()) return
    Text("이번 주 도전 $done/${challenges.size}", style = MaterialTheme.typography.labelLarge)
    challenges.forEach { c ->
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

@Composable
private fun BadgeLine(profile: GameProfile) {
    val earned = profile.earnedBadges
    val next = profile.nextBadges.firstOrNull()
    if (earned.isEmpty() && next == null) return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(Icons.Default.MilitaryTech, contentDescription = profile.style.badgeWord, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
        Text(
            listOfNotNull(
                earned.takeLast(RECENT_BADGES).joinToString(" · ") { it.badge.label }.takeIf { it.isNotBlank() },
                next?.let { "다음: ${it.badge.label}" },
            ).joinToString(" · "),
            style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RewardLine(r: RewardView) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(Icons.Default.CardGiftcard, contentDescription = "보상", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Text(
            if (r.status == RewardStatus.EARNED) "받을 차례 · ${r.reward.title}" else "${r.condition} · ${r.reward.title}",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val RECENT_BADGES = 2
private const val TRACK_ALPHA = 0.15f
private const val STICKERS_PER_ROW = 5
private const val STICKER_DP = 34
private const val MINUTES_PER_HOUR = 60
