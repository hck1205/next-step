package com.nextstep.app.ui.familytalk.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.nextstep.app.ui.familytalk.FamilyTalkUiState

/** 주말 이야기 네 단계: ✨ 반짝인 순간 → 🏅 자랑 → 🎈 기대되는 일 → 🎁 가족 즐거움. 고르지 않고 넘어가도 됩니다. */
@Composable
internal fun TalkSteps(state: FamilyTalkUiState, onSave: (String, String, String) -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var proud by rememberSaveable(state.saved?.id) { mutableStateOf(state.saved?.proud.orEmpty()) }
    var wish by rememberSaveable(state.saved?.id) { mutableStateOf(state.saved?.wish.orEmpty()) }
    var treat by rememberSaveable(state.saved?.id) { mutableStateOf(state.saved?.treat.orEmpty()) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StepDots(step)
        when (step) {
            0 -> SparkleStep(state.highlights, state.studentName)
            1 -> ChoiceStep("🏅 이번 주 가장 자랑하고 싶은 것", "하나만 골라도, 직접 적어도 좋아요", state.proudIdeas, proud) { proud = it }
            2 -> ForwardStep(state, wish) { wish = it }
            else -> ChoiceStep("🎁 다음 주 우리 가족 작은 즐거움", "함께 하면 기다려지는 것 하나", FamilyTalk.TREAT_IDEAS, treat) { treat = it }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (step > 0) TextButton(onClick = { step-- }) { Text("이전") }
            Spacer(Modifier.weight(1f))
            if (step < LAST) Button(onClick = { step++ }) { Text("다음") } else Button(onClick = { onSave(proud, wish, treat); step = 0 }) { Text("다 됐어요 🎉") }
        }
    }
}

@Composable
private fun StepDots(step: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        (0..LAST).forEach { i ->
            val color = if (i <= step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            Box(Modifier.size(if (i == step) 10.dp else 8.dp).background(color, CircleShape))
        }
        Text("  ${step + 1}/${LAST + 1}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private const val LAST = 3
