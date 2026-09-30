package com.nextstep.app.ui.familytalk.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.familytalk.FamilyTalk
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.familytalk.FamilyTalkUiState

/** 주말 이야기 네 걸음: 반짝인 순간 → 자랑 → 해 보고 싶은 것 → 가족 즐거움(기다리는 일과 함께). 고르지 않고 넘어가도 됩니다. */
@Composable
internal fun TalkSteps(state: FamilyTalkUiState, onSave: (String, String, String) -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var proud by rememberSaveable(state.saved?.id) { mutableStateOf(state.saved?.proud.orEmpty()) }
    var wish by rememberSaveable(state.saved?.id) { mutableStateOf(state.saved?.wish.orEmpty()) }
    var treat by rememberSaveable(state.saved?.id) { mutableStateOf(state.saved?.treat.orEmpty()) }
    val weekLabel = "${DateUtils.formatShortDate(state.week)} – ${DateUtils.formatShortDate(state.week.plusDays(LAST_DAY))}"
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TalkPath(step)
        when (step) {
            0 -> SparkleStep(state.highlights, weekLabel)
            1 -> ChoiceStep("가장 자랑하고 싶은 건?", "이번 주에 해낸 것 가운데 하나만 골라요", Icons.Filled.EmojiEvents, state.proudIdeas, proud, { proud = it }, wide = true)
            2 -> ChoiceStep("다음 주에 해 보고 싶은 것", "해야 할 일이 아니라, 해 보면 신날 것", Icons.Filled.Eco, FamilyTalk.WISH_IDEAS, wish, { wish = it })
            else -> TreatStep(state, treat) { treat = it }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (step > 0) OutlinedButton(onClick = { step-- }) { Text("이전") }
            Spacer(Modifier.weight(1f))
            if (step < LAST) Button(onClick = { step++ }) { Text("다음") } else Button(onClick = { onSave(proud, wish, treat); step = 0 }) { Text("다 됐어요") }
        }
    }
}

private const val LAST = 3
private const val LAST_DAY = 6L
