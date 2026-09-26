package com.nextstep.app.ui.kidme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.card.BadgeGrid
import com.nextstep.app.ui.components.card.GameCard
import com.nextstep.app.ui.components.row.RewardRow
import com.nextstep.app.ui.kidme.components.StickerGrid

/**
 * 아이용 "나" 탭(학령 전·초1~2): 기록 허브 대신 스티커판 하나.
 * 공부한 날 별, 활동한 날 꽃, 이번 주 끝낸 할 일 체크, 최근에 한 것. 글보다 그림, 비교 없음.
 */
@Composable
fun KidMeScreen(viewModel: KidMeViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    KidMeContent(state)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KidMeContent(state: KidMeUiState) {
    Scaffold(topBar = { TopAppBar(title = { Text(if (state.studentName.isBlank()) "내 스티커" else "${state.studentName}의 스티커") }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.game?.let { g ->
                item { GameCard(g, state.nextReward, showsNumbers = false) }
                item { SectionTitle(g.style.badgeWord) }
                item { BadgeGrid(g.badges, showsNumbers = false) }
            }
            if (state.rewards.isNotEmpty()) {
                item { SectionTitle("약속한 선물") }
                item { AppCard { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { state.rewards.forEach { RewardRow(it) } } } }
            }
            val board = state.board
            if (board == null) {
                item { AppCard { EmptyState("공부하거나 활동하면 스티커가 생겨요") } }
                return@LazyColumn
            }
            item {
                AppCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(32.dp))
                            Text("스티커 붙인 날 ${board.stickers}일", style = MaterialTheme.typography.titleLarge)
                        }
                        StickerGrid(board.days, state.today)
                    }
                }
            }
            item {
                AppCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("이번 주에 끝낸 할 일", style = MaterialTheme.typography.titleMedium)
                        if (board.doneThisWeek == 0) Text("하나 끝내면 여기에 체크가 생겨요", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        else Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(board.doneThisWeek.coerceAtMost(MAX_CHECKS)) { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp)) }
                        }
                    }
                }
            }
            if (state.recentActivities.isNotEmpty()) {
                item { SectionTitle("내가 한 것") }
                items(state.recentActivities, key = { it.id }) { a ->
                    AppCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(28.dp))
                            Text(a.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text(DateUtils.formatDate(DateUtils.fromEpochDay(a.date)), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

private const val MAX_CHECKS = 10
