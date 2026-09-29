package com.nextstep.app.ui.familytalk.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.domain.familycalendar.FamilyOccurrence
import com.nextstep.app.ui.components.card.AppCard

/** 나눈 이야기 요약: 자랑 · 해 보고 싶은 것 · 가족 즐거움 · 다음 주 첫 즐거운 일정. [onEdit] 이 있으면 "다시 이야기하기". */
@Composable
internal fun TalkSummaryCard(saved: WeekPlanEntity, next: FamilyOccurrence?, onEdit: (() -> Unit)?) {
    val lines = listOfNotNull(
        saved.proud.takeIf { it.isNotBlank() }?.let { "🏅 자랑: $it" },
        saved.wish.takeIf { it.isNotBlank() }?.let { "🌱 해 보고 싶은 것: $it" },
        saved.treat.takeIf { it.isNotBlank() }?.let { "🎁 가족 즐거움: $it" },
        next?.let { "🎈 기다리는 일: ${it.event.title}" },
    )
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("다음 주가 기다려져요 🎈", style = MaterialTheme.typography.titleMedium)
            if (lines.isEmpty()) Text("이번 주 이야기를 나눴어요", style = MaterialTheme.typography.bodyLarge)
            lines.forEach { Text(it, style = MaterialTheme.typography.bodyLarge) }
            if (onEdit != null) TextButton(onClick = onEdit) { Text("다시 이야기하기") }
        }
    }
}
