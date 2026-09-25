package com.nextstep.app.ui.kidfamily

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.cheer.KidMessage
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.row.NoteRow
import com.nextstep.app.ui.kidfamily.components.ParentGateDialog

/**
 * 아이용 "가족" 탭(학령 전~초4): 설정 대신 가족 얼굴과 한 번 누르는 말. 연결 코드·과목·계정 같은 어른용 설정은
 * 맨 아래 "어른 설정"에서 곱셈 하나(어른 확인)를 맞힌 뒤에만 열립니다.
 */
@Composable
fun KidFamilyScreen(actions: KidFamilyActions, viewModel: KidFamilyViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    KidFamilyContent(state = state, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KidFamilyContent(state: KidFamilyUiState, actions: KidFamilyActions, onEvent: (KidFamilyEvent) -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(state.unlocked) {
        if (state.unlocked) { onEvent(KidFamilyEvent.ConsumeUnlock); actions.onOpenSettings() }
    }
    LaunchedEffect(state.sentMessage) {
        state.sentMessage?.let {
            android.widget.Toast.makeText(context, "\"$it\" 보냈어요", android.widget.Toast.LENGTH_SHORT).show()
            onEvent(KidFamilyEvent.ClearSent)
        }
    }
    state.gate?.let { gate ->
        ParentGateDialog(gate, wrong = state.gateError, onAnswer = { onEvent(KidFamilyEvent.AnswerGate(it)) }, onDismiss = { onEvent(KidFamilyEvent.CloseGate) })
    }

    Scaffold(topBar = { TopAppBar(title = { Text("가족") }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    state.family.forEach { m ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(FACE_DP.dp).background(if (m.isParent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary, CircleShape), contentAlignment = Alignment.Center) {
                                Text(m.roleLabel.take(1), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onTertiary)
                            }
                            Text(m.roleLabel, style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }
            item { SectionTitle("눌러서 보내기") }
            items(KidMessage.entries.chunked(COLUMNS)) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { msg ->
                        Box(
                            Modifier.weight(1f).heightIn(min = BUTTON_DP.dp).clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer).clickable { onEvent(KidFamilyEvent.Send(msg)) }
                                .semantics { role = Role.Button }.padding(12.dp),
                            contentAlignment = Alignment.Center,
                        ) { Text(msg.text, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onPrimaryContainer) }
                    }
                    if (row.size < COLUMNS) Spacer(Modifier.weight(1f))
                }
            }
            if (state.notes.isNotEmpty()) {
                item { SectionTitle("가족이 남긴 말") }
                items(state.notes, key = { it.id }) { n -> NoteRow(n, onDelete = null) }
            }
            item {
                AppCard(onClick = { onEvent(KidFamilyEvent.OpenSettings) }) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("어른 설정", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onEvent(KidFamilyEvent.OpenSettings) }) { Text("열기") }
                    }
                }
            }
        }
    }
}

private const val FACE_DP = 64
private const val BUTTON_DP = 72
private const val COLUMNS = 2
